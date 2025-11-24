async function translateText() {
    const text = document.getElementById('inputText').value;
    const resultDiv = document.getElementById('result');
    
    if (!text.trim()) {
        alert("Please enter some text.");
        return;
    }

    resultDiv.innerText = "Translating...";

    try {
        const response = await fetch('/api/translate', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ text: text })
        });

        if (!response.ok) {
            throw new Error('Network response was not ok');
        }

        const data = await response.json();
        resultDiv.innerText = data.translation;
    } catch (error) {
        resultDiv.innerText = "Error: " + error.message;
        console.error('Error:', error);
    }
}
