# Moroccan-Arabic-Translator

A Spring Boot RESTful web service that translates English text to Moroccan Arabic (Darija) using the **Google Gemini 2.5 Flash** (Stable) LLM.

## Features
- **Model:** Uses `gemini-2.5-flash` for fast and free translations.
- **Architecture:** Spring Boot + Spring MVC / JAX-RS.
- **Structure:** Organized into clean packages (`controllers`, `services`).
- **Frontend:** Minimal HTML/CSS/JS interface.

## Architecture Components

The application is divided into two main interaction points, handled by specific controllers:

### 1. The Client representer (`WebController`)
*   **Role:** Serves the User Interface.
*   **Class:** `WebController.java`
*   **Function:** When you visit `http://localhost:1000/`, this controller delivers the `index.html` page. This HTML page (along with `script.js`) acts as the **Client** that consumes the API.

### 2. The Web Service (`TranslatorRestController`)
*   **Role:** Provides the REST API.
*   **Class:** `TranslatorRestController.java`
*   **Function:** This is the actual engine. It accepts JSON data at `/api/translate`, processes it using the `TranslationService`, and returns the result. It doesn't care about the UI; it only deals with raw data.

## Prerequisites

- Java 17 or later
- Maven 3.8+
- A Google Gemini API Key (Get one for free from [Google AI Studio](https://aistudio.google.com/app/apikey))

## Configuration

You must provide your Gemini API Key.

**Method 1: Environment Variable (Recommended)**
Linux/Mac:
```bash
export GEMINI_API_KEY=your_api_key_here
```
Windows (PowerShell):
```powershell
$env:GEMINI_API_KEY="your_api_key_here"
```

**Method 2: Application Properties**
Open `src/main/resources/application.properties` and replace the placeholder:
```properties
gemini.api.key=your_api_key_here
```

## Running the Application

Run the following command in the project root:

```bash
mvn spring-boot:run
```

The application will start on `http://localhost:1000`.

## Usage

### Web UI
Open `http://localhost:1000` in your browser.

### REST API
Endpoint: `POST /api/translate`

**cURL Example:**
```bash
curl -X POST -H "Content-Type: application/json" -d '{"text":"Hello friend"}' http://localhost:1000/api/translate
```

**Response:**
```json
{
  "translation": "أهلا صاحبي"
}
```