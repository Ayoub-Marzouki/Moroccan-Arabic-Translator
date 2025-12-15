// --- Animations ---
const typingText = document.getElementById('typing-text');
const words = ["Darija", "Moroccan", "Locally"];
let wordIndex = 0;
let charIndex = 0;
let isDeleting = false;
let typeSpeed = 100;

function typeEffect() {
    const currentWord = words[wordIndex];
    
    if (isDeleting) {
        typingText.textContent = currentWord.substring(0, charIndex - 1);
        charIndex--;
        typeSpeed = 50;
    } else {
        typingText.textContent = currentWord.substring(0, charIndex + 1);
        charIndex++;
        typeSpeed = 150;
    }

    if (!isDeleting && charIndex === currentWord.length) {
        isDeleting = true;
        typeSpeed = 2000; // Pause at end
    } else if (isDeleting && charIndex === 0) {
        isDeleting = false;
        wordIndex = (wordIndex + 1) % words.length;
        typeSpeed = 500;
    }

    setTimeout(typeEffect, typeSpeed);
}

document.addEventListener('DOMContentLoaded', () => {
    typeEffect();
    
    // Intersection Observer for fade-in animations
    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.style.opacity = '1';
                entry.target.style.transform = 'translateY(0)';
            }
        });
    }, { threshold: 0.1 });

    document.querySelectorAll('.fade-in').forEach(el => {
        el.style.opacity = '0';
        el.style.transform = 'translateY(20px)';
        el.style.transition = 'opacity 0.8s ease, transform 0.8s ease';
        observer.observe(el);
    });
});

// --- Translation Logic ---
async function translateText() {
    const input = document.getElementById('inputText');
    const resultDiv = document.getElementById('result');
    const btn = document.querySelector('.translate-btn');
    const originalBtnText = btn.innerHTML;

    if (!input.value.trim()) {
        shakeElement(input);
        return;
    }

    // Loading State
    btn.innerHTML = '<span class="loader"></span> Translating...'; // Simple text loader for now
    btn.disabled = true;
    resultDiv.style.opacity = '0.5';

    try {
        const response = await fetch('/api/translate', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Basic ' + btoa('user:password')
            },
            body: JSON.stringify({ text: input.value })
        });

        if (!response.ok) throw new Error('Translation failed');

        const data = await response.json();
        
        // Typewriter effect for result
        resultDiv.textContent = "";
        resultDiv.style.opacity = '1';
        let i = 0;
        const typeResult = () => {
            if (i < data.translation.length) {
                resultDiv.textContent += data.translation.charAt(i);
                i++;
                setTimeout(typeResult, 30);
            }
        };
        typeResult();

    } catch (error) {
        resultDiv.textContent = "Error: Could not connect to the translation neural network.";
        resultDiv.style.color = "#ff5f56";
    } finally {
        btn.innerHTML = originalBtnText;
        btn.disabled = false;
    }
}

function shakeElement(element) {
    element.style.transform = "translateX(10px)";
    setTimeout(() => {
        element.style.transform = "translateX(-10px)";
        setTimeout(() => {
            element.style.transform = "translateX(0)";
        }, 100);
    }, 100);
}

let currentAudio = null;

// --- Text-to-Speech ---
async function speakText() {
    const text = document.getElementById('result').innerText;
    if (!text || text.startsWith("Error:") || text === "Translating...") return;

    // Stop any currently playing audio
    if (currentAudio) {
        currentAudio.pause();
        currentAudio.currentTime = 0;
    }

    try {
        const response = await fetch('/api/tts', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Basic ' + btoa('user:password')
            },
            body: JSON.stringify({ text: text })
        });

        if (!response.ok) throw new Error("TTS request failed");

        const blob = await response.blob();
        const url = URL.createObjectURL(blob);
        
        currentAudio = new Audio(url);
        currentAudio.play();
    } catch (e) {
        console.error("Audio playback error:", e);
        alert("Could not play audio.");
    }
}

// --- Utils ---
function copyCode() {
    const code = `curl -u user:password -X POST \
  -H "Content-Type: application/json" \
  -d "{\"text\":\"Hello friend\"}" \
  http://localhost:1000/api/translate`;
    
    navigator.clipboard.writeText(code).then(() => {
        const btn = document.querySelector('.copy-btn');
        btn.textContent = "Copied!";
        setTimeout(() => btn.textContent = "Copy", 2000);
    });
}