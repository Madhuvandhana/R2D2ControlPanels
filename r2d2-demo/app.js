/**
 * R2-D2 ESP32 Controller Simulation Logic
 */

// Web Audio API Context
let audioCtx = null;
let soundEnabled = true;

function initAudio() {
    if (!audioCtx) {
        audioCtx = new (window.AudioContext || window.webkitAudioContext)();
    }
}

// Generate R2-D2-like telemetry sounds dynamically
function playR2Sound(type) {
    if (!soundEnabled) return;
    initAudio();
    if (audioCtx.state === 'suspended') {
        audioCtx.resume();
    }

    const now = audioCtx.currentTime;
    
    switch (type) {
        case 'happy':
            // High-pitched playful whistles (chirp up/down rapidly)
            playChirp(now, 800, 1600, 0.15, 'triangle');
            playChirp(now + 0.12, 1600, 1200, 0.1, 'square');
            playChirp(now + 0.22, 1200, 2200, 0.2, 'sine');
            break;
        case 'sad':
            // Low-pitched mournful moan
            playChirp(now, 500, 200, 0.5, 'sine');
            playChirp(now + 0.1, 450, 150, 0.45, 'triangle');
            break;
        case 'scan':
            // Rapid telemetry data scan
            for (let i = 0; i < 6; i++) {
                const start = now + (i * 0.08);
                const freq = 900 + Math.random() * 1200;
                playChirp(start, freq, freq + (Math.random() > 0.5 ? 200 : -200), 0.06, 'square');
            }
            break;
        case 'beam':
            // Electric hum & charge up sound
            const osc = audioCtx.createOscillator();
            const gain = audioCtx.createGain();
            osc.connect(gain);
            gain.connect(audioCtx.destination);
            osc.type = 'sawtooth';
            osc.frequency.setValueAtTime(100, now);
            osc.frequency.exponentialRampToValueAtTime(1200, now + 0.4);
            gain.gain.setValueAtTime(0.15, now);
            gain.gain.exponentialRampToValueAtTime(0.01, now + 0.5);
            osc.start(now);
            osc.stop(now + 0.5);
            break;
        case 'click':
            // Simple notification tap
            playChirp(now, 1000, 1000, 0.03, 'sine');
            break;
        case 'connect':
            // Astromech boot-up signature sound
            playChirp(now, 600, 1200, 0.15, 'sine');
            playChirp(now + 0.15, 1200, 800, 0.1, 'triangle');
            playChirp(now + 0.25, 800, 1500, 0.25, 'sine');
            break;
    }
}

function playChirp(startTime, startFreq, endFreq, duration, waveType) {
    const osc = audioCtx.createOscillator();
    const gain = audioCtx.createGain();
    
    osc.connect(gain);
    gain.connect(audioCtx.destination);
    
    osc.type = waveType || 'sine';
    osc.frequency.setValueAtTime(startFreq, startTime);
    osc.frequency.exponentialRampToValueAtTime(endFreq, startTime + duration);
    
    gain.gain.setValueAtTime(0.12, startTime);
    gain.gain.exponentialRampToValueAtTime(0.001, startTime + duration);
    
    osc.start(startTime);
    osc.stop(startTime + duration);
}

// DOM Elements Initialization
const connectBtn = document.getElementById('connect-btn');
const connectionPill = document.getElementById('connection-pill');
const bleIndicator = document.getElementById('ble-indicator');
const customColorPicker = document.getElementById('custom-color-picker');
const colorPresets = document.querySelectorAll('.color-preset-btn');
const psiLedLight = document.getElementById('psi-led-light');
const psiGlowMid = document.getElementById('psi-glow-mid');
const joystickKnob = document.getElementById('joystick-knob');
const holoCoords = document.getElementById('holo-coords');
const beamBtn = document.getElementById('beam-btn');
const resetHoloBtn = document.getElementById('reset-holo-btn');
const frontLogicInput = document.getElementById('front-logic-input');
const rearLogicInput = document.getElementById('rear-logic-input');
const frontLogicMatrix = document.getElementById('front-logic-matrix');
const rearLogicMatrix = document.getElementById('rear-logic-matrix');
const r2d2Svg = document.getElementById('r2d2-svg');
const consoleLogs = document.getElementById('console-logs');
const hologramScreen = document.getElementById('hologram-screen');
const toggleAudioBtn = document.getElementById('toggle-audio-btn');

// Pie Panel Switches
const panelTop1 = document.getElementById('panel-top1');
const panelTop2 = document.getElementById('panel-top2');
const panelRear1 = document.getElementById('panel-rear1');

// Connection State
let isConnected = true;

// Logger function
function logESP32(message, level = 'green') {
    const time = new Date().toLocaleTimeString().split(' ')[0];
    const logLine = document.createElement('div');
    logLine.className = `line ${level}`;
    logLine.innerText = `[${time}] ${message}`;
    consoleLogs.appendChild(logLine);
    consoleLogs.scrollTop = consoleLogs.scrollHeight;
}

// Connection toggle
connectBtn.addEventListener('click', () => {
    isConnected = !isConnected;
    playR2Sound('click');
    if (isConnected) {
        connectBtn.innerText = 'Disconnect Device';
        connectBtn.className = 'app-btn connected-btn';
        connectionPill.innerText = 'CONNECTED';
        connectionPill.className = 'connection-pill connected';
        bleIndicator.className = 'bluetooth-icon active';
        logESP32('BLE Connection re-established by client.', 'blue');
        playR2Sound('connect');
    } else {
        connectBtn.innerText = 'Pair & Connect BLE';
        connectBtn.className = 'app-btn';
        connectionPill.innerText = 'DISCONNECTED';
        connectionPill.className = 'connection-pill disconnected';
        bleIndicator.className = 'bluetooth-icon';
        logESP32('Client requested BLE disconnect. Entering low power advertising.', 'yellow');
        playR2Sound('sad');
    }
});

// Sound Toggle
toggleAudioBtn.addEventListener('click', () => {
    soundEnabled = !soundEnabled;
    toggleAudioBtn.innerText = soundEnabled ? '🔊 Mute Sound' : '🔇 Unmute Sound';
    toggleAudioBtn.classList.toggle('highlight', !soundEnabled);
});

// Color Setups
function updatePSIColor(hexColor) {
    if (!isConnected) return;
    // Map preset values or change directly
    psiGlowMid.setAttribute('stop-color', hexColor);
    logESP32(`[ESP32 BLE] RX PACKET (CMD_LED): Red/Green/Blue updated to color Hex ${hexColor}`, 'purple');
    playR2Sound('click');
}

colorPresets.forEach(btn => {
    btn.addEventListener('click', (e) => {
        colorPresets.forEach(b => b.classList.remove('active'));
        btn.classList.add('active');
        const color = btn.getAttribute('data-color');
        customColorPicker.value = color;
        updatePSIColor(color);
    });
});

customColorPicker.addEventListener('input', (e) => {
    colorPresets.forEach(b => b.classList.remove('active'));
    updatePSIColor(e.target.value);
});

// Holo Projector Joystick & Controls
const holoEye = document.getElementById('holo-eye');
const holoBeamRay = document.getElementById('holo-beam-ray');
let holoX = 0;
let holoY = 0;
let isHoloActive = false;

function updateHoloProjector(x, y) {
    if (!isConnected) return;
    // Limit to bounds of joystick movement (-40 to 40)
    holoX = Math.round(x);
    holoY = Math.round(y);
    
    // Apply SVG translations to simulate 3D socket rotation
    // Translate eye ball slightly to match joystick coordinates
    const moveX = (holoX / 40) * 8;
    const moveY = (holoY / 40) * 6;
    holoEye.setAttribute('transform', `translate(${moveX}, ${moveY})`);
    
    // Adjust holo projection beam skew/angle based on coords
    const endX1 = 100 + moveX * 4;
    const endX2 = 300 + moveX * 4;
    const endY = 380 + moveY * 2;
    holoBeamRay.setAttribute('points', `${200 + moveX},${220 + moveY} ${endX1},${endY} ${endX2},${endY}`);
    
    holoCoords.innerText = `X: ${holoX}, Y: ${holoY}`;
}

// Recenter joystick helper
function recenterJoystick() {
    joystickKnob.style.transform = 'translate(0px, 0px)';
    updateHoloProjector(0, 0);
    logESP32('[ESP32 BLE] RX PACKET (CMD_HOLO_CENTER): Projector returned to home position (0, 0).', 'blue');
}

// Direction keys click handler
document.getElementById('joy-up').addEventListener('click', () => { moveJoy(0, -25); });
document.getElementById('joy-down').addEventListener('click', () => { moveJoy(0, 25); });
document.getElementById('joy-left').addEventListener('click', () => { moveJoy(-25, 0); });
document.getElementById('joy-right').addEventListener('click', () => { moveJoy(25, 0); });

function moveJoy(dx, dy) {
    playR2Sound('click');
    let x = holoX + dx;
    let y = holoY + dy;
    x = Math.max(-40, Math.min(40, x));
    y = Math.max(-40, Math.min(40, y));
    joystickKnob.style.transform = `translate(${x}px, ${y}px)`;
    updateHoloProjector(x, y);
    logESP32(`[ESP32 BLE] RX PACKET (CMD_HOLO_MOVE): Servos updated to angles X:${x + 90}° Y:${y + 90}°`, 'blue');
}

resetHoloBtn.addEventListener('click', () => {
    playR2Sound('click');
    recenterJoystick();
});

// Joystick Drag and Drop logic
let isDragging = false;
let startDragX = 0;
let startDragY = 0;

joystickKnob.addEventListener('mousedown', (e) => {
    isDragging = true;
    startDragX = e.clientX;
    startDragY = e.clientY;
    joystickKnob.style.transition = 'none';
});

document.addEventListener('mousemove', (e) => {
    if (!isDragging) return;
    const dx = e.clientX - startDragX;
    const dy = e.clientY - startDragY;
    
    // Boundary circle of radius 40
    const distance = Math.sqrt(dx * dx + dy * dy);
    let targetX = dx;
    let targetY = dy;
    if (distance > 40) {
        targetX = (dx / distance) * 40;
        targetY = (dy / distance) * 40;
    }
    
    joystickKnob.style.transform = `translate(${targetX}px, ${targetY}px)`;
    updateHoloProjector(targetX, targetY);
});

document.addEventListener('mouseup', () => {
    if (isDragging) {
        isDragging = false;
        joystickKnob.style.transition = 'transform 0.2s ease-out';
        logESP32(`[ESP32 BLE] RX PACKET (CMD_HOLO_MOVE): Drag coordinate release - X:${holoX}, Y:${holoY}`, 'blue');
        playR2Sound('scan');
    }
});

// Toggle Hologram Projection
beamBtn.addEventListener('click', () => {
    isHoloActive = !isHoloActive;
    playR2Sound('beam');
    if (isHoloActive) {
        beamBtn.classList.add('active-glowing');
        beamBtn.innerText = 'Kill Hologram Projector';
        holoBeamRay.style.opacity = '1';
        hologramScreen.classList.add('active');
        logESP32('[ESP32 BLE] RX PACKET (CMD_HOLO_BEAM): Laser diode voltage set to HIGH (5V)', 'yellow');
    } else {
        beamBtn.classList.remove('active-glowing');
        beamBtn.innerText = 'Toggle Holo Projection';
        holoBeamRay.style.opacity = '0';
        hologramScreen.classList.remove('active');
        logESP32('[ESP32 BLE] RX PACKET (CMD_HOLO_BEAM): Laser diode voltage set to LOW (0V)', 'yellow');
    }
});

hologramScreen.addEventListener('click', () => {
    // Click overlay to close
    isHoloActive = false;
    beamBtn.classList.remove('active-glowing');
    beamBtn.innerText = 'Toggle Holo Projection';
    holoBeamRay.style.opacity = '0';
    hologramScreen.classList.remove('active');
    playR2Sound('click');
});

// Utility Panels
function updatePanels() {
    if (!isConnected) return;
    const isTop1 = panelTop1.checked;
    const isTop2 = panelTop2.checked;
    const isRear1 = panelRear1.checked;
    
    // Toggle class on R2 dome element to rotate pieces
    if (isTop1 || isTop2 || isRear1) {
        r2d2Svg.classList.add('panel-open');
    } else {
        r2d2Svg.classList.remove('panel-open');
    }
    
    // Control specific path visibility
    document.getElementById('pie-panel-top1').style.transform = isTop1 ? 'translate(-10px, -20px) rotate(-15deg)' : 'none';
    document.getElementById('pie-panel-top2').style.transform = isTop2 ? 'translate(10px, -20px) rotate(15deg)' : 'none';
    document.getElementById('pie-panel-rear1').style.transform = isRear1 ? 'translate(0px, -25px) scaleY(0.7)' : 'none';
    
    logESP32(`[ESP32 BLE] RX PACKET (CMD_PIE_PANELS): Flaps state updated [Top1:${isTop1 ? 'OPEN' : 'CLOSE'} | Top2:${isTop2 ? 'OPEN' : 'CLOSE'} | Rear:${isRear1 ? 'OPEN' : 'CLOSE'}]`, 'blue');
    playR2Sound('happy');
}

panelTop1.addEventListener('change', updatePanels);
panelTop2.addEventListener('change', updatePanels);
panelRear1.addEventListener('change', updatePanels);

// Logic Matrix Text Changes
function updateLogicDisplay(position, text) {
    if (!isConnected) return;
    if (position === 'front') {
        frontLogicMatrix.innerText = text || ' ';
        logESP32(`[ESP32 BLE] RX PACKET (CMD_MATRIX_FRONT): Scroll custom text: "${text}"`, 'green');
    } else if (position === 'rear') {
        rearLogicMatrix.innerText = text || ' ';
        logESP32(`[ESP32 BLE] RX PACKET (CMD_MATRIX_REAR): Scroll custom text: "${text}"`, 'green');
    }
    playR2Sound('click');
}

frontLogicInput.addEventListener('input', (e) => {
    updateLogicDisplay('front', e.target.value);
});

rearLogicInput.addEventListener('input', (e) => {
    updateLogicDisplay('rear', e.target.value);
});


/** ==========================================
 *  WALKTHROUGH TIMELINE AUTOMATION SIMULATOR
 *  (Simulates a dynamic video review demo)
 *  ========================================== */

const walkthroughBar = document.getElementById('walkthrough-bar');
const wtInstruction = document.getElementById('walkthrough-instruction');
const wtPlayPause = document.getElementById('wt-play-pause');
const wtPrev = document.getElementById('wt-prev');
const wtNext = document.getElementById('wt-next');
const wtClose = document.getElementById('wt-close');
const timelineBar = document.getElementById('timeline-bar');

const tourSteps = [
    {
        title: "BLE Connection Handshake",
        description: "The Android app pairs with the ESP32 transceiver using Bluetooth Low Energy. Toggling connection connects the logic registers immediately.",
        action: () => {
            if (!isConnected) connectBtn.click();
            logESP32("[DEMO] Establishing secure BLE pairing...");
        }
    },
    {
        title: "Adjusting Primary PSI LEDs",
        description: "Selecting a color profile sends serialized hexadecimal packets directly to the WS2812B LEDs inside the dome. Let's switch colors!",
        action: () => {
            const greenPreset = document.querySelector('.color-preset-btn[data-color="#00ff66"]');
            greenPreset.click();
        }
    },
    {
        title: "Panning the Holo Projector",
        description: "Using the touch coordinates, the app updates duty-cycle micro-servo signals. Here, we pan the lens to coordinate (25, -25).",
        action: () => {
            moveJoy(25, -25);
        }
    },
    {
        title: "Projecting Holograms",
        description: "Engaging holographic beam toggles the laser light ray and displays high-voltage archives on target display screen overlays.",
        action: () => {
            if (!isHoloActive) beamBtn.click();
        }
    },
    {
        title: "Utility Flaps & Pie Panels",
        description: "Toggling mechanical panel switches deploys R2-D2's top sensors. Watch the SVG model open the Radar Scanner panels!",
        action: () => {
            if (isHoloActive) beamBtn.click(); // Close hologram first
            panelTop1.checked = true;
            panelTop2.checked = true;
            updatePanels();
        }
    },
    {
        title: "Logic Displays & Scrolling Text",
        description: "Finally, entering text uploads customized string payloads scrolling across R2's diagnostic displays in real-time.",
        action: () => {
            // Restore dome panels
            panelTop1.checked = false;
            panelTop2.checked = false;
            updatePanels();
            
            // Set text
            frontLogicInput.value = "OBI-WAN";
            updateLogicDisplay('front', "OBI-WAN");
            rearLogicInput.value = "HELP ME";
            updateLogicDisplay('rear', "HELP ME");
        }
    }
];

let currentStep = 0;
let isPlaying = false;
let playInterval = null;

function showStep(stepIndex) {
    if (stepIndex < 0 || stepIndex >= tourSteps.length) return;
    currentStep = stepIndex;
    
    // Execute action
    tourSteps[currentStep].action();
    
    // Update instruction texts
    wtInstruction.innerHTML = `<strong>Step ${currentStep + 1} of ${tourSteps.length}: ${tourSteps[currentStep].title}</strong> - ${tourSteps[currentStep].description}`;
    
    // Update timeline bar progress
    const pct = ((currentStep + 1) / tourSteps.length) * 100;
    timelineBar.style.width = `${pct}%`;
}

function startAutoplay() {
    isPlaying = true;
    wtPlayPause.innerText = "Pause";
    wtPlayPause.classList.add('highlight');
    
    playInterval = setInterval(() => {
        if (currentStep < tourSteps.length - 1) {
            showStep(currentStep + 1);
        } else {
            stopAutoplay();
            logESP32("[DEMO] Guided Walkthrough completed successfully.");
            playR2Sound('happy');
        }
    }, 4500); // 4.5 seconds per step
}

function stopAutoplay() {
    isPlaying = false;
    wtPlayPause.innerText = "Play";
    wtPlayPause.classList.remove('highlight');
    if (playInterval) {
        clearInterval(playInterval);
        playInterval = null;
    }
}

// Event Bindings
document.getElementById('start-walkthrough-btn').addEventListener('click', () => {
    walkthroughBar.style.display = 'flex';
    currentStep = 0;
    showStep(0);
    startAutoplay();
    playR2Sound('connect');
});

wtPlayPause.addEventListener('click', () => {
    playR2Sound('click');
    if (isPlaying) {
        stopAutoplay();
    } else {
        startAutoplay();
    }
});

wtPrev.addEventListener('click', () => {
    playR2Sound('click');
    stopAutoplay();
    if (currentStep > 0) {
        showStep(currentStep - 1);
    }
});

wtNext.addEventListener('click', () => {
    playR2Sound('click');
    stopAutoplay();
    if (currentStep < tourSteps.length - 1) {
        showStep(currentStep + 1);
    }
});

wtClose.addEventListener('click', () => {
    playR2Sound('click');
    stopAutoplay();
    walkthroughBar.style.display = 'none';
    recenterJoystick();
});

// Autostart boot logs & sounds
setTimeout(() => {
    playR2Sound('connect');
    logESP32("R2-D2 Astromech Emulator ready. Use controls or start Tour!");
}, 500);
