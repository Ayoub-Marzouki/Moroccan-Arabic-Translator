document.getElementById('translateBtn').addEventListener('click', async () => {
    const inputText = document.getElementById('inputText').value;
    const resultDiv = document.getElementById('result');

    if (!inputText.trim()) {
        resultDiv.textContent = "Please enter some text.";
        return;
    }

    resultDiv.textContent = "Translating...";
    resultDiv.classList.remove('error');

    try {
        const response = await fetch('http://127.0.0.1:1000/api/translate', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': 'Basic ' + btoa('user:password')
            },
            body: JSON.stringify({ text: inputText })
        });

        if (!response.ok) {
            throw new Error(`Server error: ${response.status}`);
        }

        const data = await response.json();
        resultDiv.textContent = data.translation;
    } catch (error) {
        console.error(error);
        resultDiv.textContent = "Error: " + error.message;
        resultDiv.classList.add('error');
    }
});
