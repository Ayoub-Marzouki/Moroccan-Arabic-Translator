# The Text-to-Speech (TTS) Implementation

This document is about the development of the Text-to-Speech feature for the Moroccan Arabic Translator, highlighting the technical challenges encountered and the solutions implemented.

## 1. Initial Approach: Frontend `SpeechSynthesis` API

**Goal:** Use the browser's native `window.speechSynthesis` API for a lightweight, client-side solution.

**Implementation:**
- Added a "Speak" button to the frontend.
- Used `SpeechSynthesisUtterance` with `lang = 'ar-SA'` (Arabic - Saudi Arabia) as a proxy for Darija.

**The Problem:**
- **Missing Voices:** Many users (especially on Windows) do not have an Arabic TTS voice installed by default.
- **Silent Failure:** If no compatible voice was found, the browser simply played nothing, confusing the user.
- **Inconsistent Quality:** Even when it worked, the voice quality varied wildly between browsers and OSs.

## 2. Pivot: Google Translate TTS (Direct Frontend)

**Goal:** Use Google's unofficial TTS API (`translate_tts`) to ensure consistent, high-quality audio for everyone.

**Implementation:**
- Constructed a URL: `https://translate.google.com/translate_tts?client=tw-ob&tl=ar&q=TEXT`
- Used `new Audio(url).play()` in the browser.

**The Problem:**
- **CORS / ORB Blocking:** Modern browsers (Chrome) blocked the request because Google does not send `Access-Control-Allow-Origin` headers. This resulted in `OpaqueResponseBlocking` (ORB) errors in the console.

## 3. The Solution: Backend Proxy

**Goal:** Bypass browser security restrictions by fetching the audio via the Java backend (`TranslatorRestController`).

**Implementation:**
- Created a new endpoint: `/api/tts`
- Frontend calls `/api/tts`, Backend calls Google, streams bytes back to Frontend.

**Challenge 3.1: 403 Forbidden / 500 Errors**
- **Cause:** Google blocked the Java `RestTemplate` default User-Agent.
- **Fix:** Spoofed the User-Agent header to look like a Chrome browser (`Mozilla/5.0...`).

**Challenge 3.2: 404 Not Found**
- **Cause:** Incorrect Spring Boot mapping. The controller was mapped to `/api/translate`, so the new method became `/api/translate/tts` instead of `/api/tts`.
- **Fix:** Changed controller mapping to `/api` and specific methods to `/translate` and `/tts`.

**Challenge 3.3: "Gibberish" / "49%" Audio**
- **Symptoms:** The audio played, but it sounded like numbers or nonsense ("O u ta ka...").
- **Root Cause:** Double-Encoding.
    1.  Frontend sent text via `GET` query param: `?text=%D8...`
    2.  Spring/Tomcat received it as `?????` (corrupted) due to default Windows charset encoding in the console/servlet.
    3.  `UriComponentsBuilder` re-encoded the corrupted string or the percent signs, causing Google to read the literal URL encoding (e.g., reading "% D 8" instead of the letter "ا").

**Challenge 3.4: "??????" Text**
- **Fix:** Switched the `/api/tts` endpoint from **GET** to **POST**.
- **Reason:** Sending text in the JSON Body allows Spring to handle UTF-8 encoding reliably, bypassing URL query string limitations.

**Challenge 3.5: Final Encoding Fix**
- **Symptoms:** Text arrived correctly in Java (verified via Hex Dump `d8a7...`), but audio was still weird.
- **Root Cause:** `UriComponentsBuilder` was interacting poorly with `RestTemplate`, potentially causing encoding mismatches for the Arabic script in the Google URL.
- **Final Fix:** Manually constructed the URL string using `URLEncoder.encode(text, StandardCharsets.UTF_8)` and passed a `URI` object to `RestTemplate`.

## 4. Final Architecture

1.  **Frontend:** Sends `POST /api/tts` with JSON `{ "text": "..." }`.
2.  **Backend:**
    -   Receives JSON (UTF-8).
    -   Manually encodes text for URL.
    -   Calls Google TTS (`client=gtx`) masquerading as Chrome.
    -   Returns `audio/mpeg` bytes.
3.  **Frontend:** Converts response to `Blob` -> `URL` -> Plays Audio.

## Key Learnings
- **Always use POST for complex text:** Query parameters are fragile with non-ASCII characters.
- **Log Hex Dumps:** Don't trust `System.out.println` for debugging encoding; check the raw bytes.
- **Proxying solves CORS:** When a 3rd party API doesn't support CORS, route it through your own backend.
- **Manual Control:** sometimes high-level builders (like `UriComponentsBuilder`) abstract away too much; manual construction is safer for strict 3rd party endpoints.
