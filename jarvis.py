import os
import re
import subprocess
import urllib.parse
import httpx
from datetime import datetime

# =========================================================
# JARVIS POWER MODE
# =========================================================

AI_MODEL = "gpt-5.6-luna"
OPENAI_URL = "https://api.openai.com/v1/responses"


# =========================================================
# SPEAK
# =========================================================

def speak(text):
    text = str(text)
    print("Jarvis:", text)

    try:
        subprocess.run(
            ["termux-tts-speak", "-l", "hi-IN", text],
            timeout=20
        )
    except Exception:
        pass


# =========================================================
# LISTEN
# =========================================================

def listen():
    try:
        result = subprocess.run(
            ["sh", "-c", "termux-speech-to-text | cat"],
            capture_output=True,
            text=True,
            timeout=30
        )

        command = result.stdout.strip()

        if not command:
            return ""

        if command == "}":
            return ""

        if command.lower() in [
            "error_no_match",
            "error",
            "null",
            "none"
        ]:
            return ""

        if command.lower().startswith("error:"):
            return ""

        print("आपने कहा:", command)
        return command

    except subprocess.TimeoutExpired:
        return ""

    except Exception as e:
        print("LISTEN ERROR:", e)
        return ""


    try:
        result = subprocess.run(
            ["sh", "-c", "termux-speech-to-text | cat"],
            capture_output=True,
            text=True,
            timeout=30
        )

        command = result.stdout.strip()

        if not command:
            return ""

        if command == "}":
            return ""

        if command.lower() in [
            "error_no_match",
            "error",
            "null",
            "none"
        ]:
            return ""

        if command.lower().startswith("error:"):
            return ""

        print("आपने कहा:", command)
        return command.lower().strip()

    except subprocess.TimeoutExpired:
        return ""

    except Exception as e:
        print("STT error:", e)
        return ""


# =========================================================
# PHONE TIME / DAY / DATE
# =========================================================

def phone_time():
    now = datetime.now()
    hour = now.strftime("%I").lstrip("0")
    minute = now.strftime("%M")
    ampm = now.strftime("%p")

    if ampm == "AM":
        ampm_hi = "AM"
    else:
        ampm_hi = "PM"

    return f"अभी समय {hour}:{minute} {ampm_hi} है।"


def phone_day():
    days = {
        "Monday": "सोमवार",
        "Tuesday": "मंगलवार",
        "Wednesday": "बुधवार",
        "Thursday": "गुरुवार",
        "Friday": "शुक्रवार",
        "Saturday": "शनिवार",
        "Sunday": "रविवार",
    }

    return "आज " + days[datetime.now().strftime("%A")] + " है।"


def phone_date():
    months = {
        1: "जनवरी",
        2: "फरवरी",
        3: "मार्च",
        4: "अप्रैल",
        5: "मई",
        6: "जून",
        7: "जुलाई",
        8: "अगस्त",
        9: "सितंबर",
        10: "अक्टूबर",
        11: "नवंबर",
        12: "दिसंबर",
    }

    now = datetime.now()

    return (
        f"आज {now.day} {months[now.month]} "
        f"{now.year} है।"
    )


# =========================================================
# OPEN URL
# =========================================================

def open_url(url):
    try:
        subprocess.run(
            ["termux-open-url", url],
            timeout=15
        )
    except Exception:
        pass


# =========================================================
# APP OPEN
# =========================================================

def open_app(package, activity=None):
    try:
        if activity:
            subprocess.run(
                ["am", "start", "--user", "0", "-n",
                 f"{package}/{activity}"],
                timeout=15
            )
        else:
            subprocess.run(
                ["monkey", "-p", package, "1"],
                timeout=15
            )

        return True

    except Exception:
        return False


# =========================================================
# WEATHER
# =========================================================

def live_weather(city="Patna"):
    try:
        url = (
            "https://wttr.in/"
            + urllib.parse.quote(city)
            + "?format=j1"
        )

        response = httpx.get(
            url,
            timeout=10,
            headers={"User-Agent": "Jarvis"}
        )

        data = response.json()
        cur = data["current_condition"][0]

        temp = cur["temp_C"]
        feels = cur["FeelsLikeC"]
        desc = cur["weatherDesc"][0]["value"]
        humidity = cur["humidity"]

        return (
            f"{city} में अभी तापमान {temp} डिग्री सेल्सियस है, "
            f"महसूस {feels} डिग्री जैसा हो रहा है। "
            f"मौसम {desc} है और नमी {humidity} प्रतिशत है।"
        )

    except Exception:
        return "माफ कीजिए, अभी live weather data नहीं मिल पाया।"


# =========================================================
# TIMER
# =========================================================

def set_timer(seconds):
    try:
        subprocess.run([
            "am",
            "start",
            "-a",
            "android.intent.action.SET_TIMER",
            "--ei",
            "android.intent.extra.alarm.LENGTH",
            str(seconds)
        ], timeout=15)

        return True

    except Exception:
        return False


# =========================================================
# NUMBER EXTRACTION
# =========================================================

def extract_phone_number(command):
    digits = re.sub(r"\D", "", command)

    # India mobile numbers
    if len(digits) == 10 and digits[0] in "6789":
        return digits

    if len(digits) == 12 and digits.startswith("91"):
        return digits[2:]

    return None


# =========================================================
# DIALER
# =========================================================

def dial_number(number):
    try:
        subprocess.run([
            "am",
            "start",
            "-a",
            "android.intent.action.DIAL",
            "-d",
            "tel:" + number
        ], timeout=15)

        return True

    except Exception:
        return False


# =========================================================
# GOOGLE SEARCH
# =========================================================

def google_search(query):
    url = (
        "https://www.google.com/search?q="
        + urllib.parse.quote(query)
    )

    open_url(url)


# =========================================================
# YOUTUBE SEARCH
# =========================================================

def youtube_search(query):
    url = (
        "https://www.youtube.com/results?search_query="
        + urllib.parse.quote(query)
    )

    open_url(url)


# =========================================================
# AI BRAIN
# =========================================================

def ai_brain(command):
    key = os.getenv("OPENAI_API_KEY", "")

    if not key:
        return (
            "मेरी AI key अभी loaded नहीं है। "
            "बाकी phone commands फिर भी काम करेंगी।"
        )

    prompt = f"""
You are Jarvis, a helpful personal Android voice assistant.

User command:
{command}

Reply briefly in natural Hindi/Hinglish.
Do not claim that you performed a phone action unless the
local Jarvis program actually performed it.
"""

    try:
        response = httpx.post(
            OPENAI_URL,
            headers={
                "Authorization": f"Bearer {key}",
                "Content-Type": "application/json"
            },
            json={
                "model": AI_MODEL,
                "input": prompt
            },
            timeout=30
        )

        if response.status_code != 200:
            return "AI service अभी available नहीं है।"

        data = response.json()

        for item in data.get("output", []):
            if item.get("type") == "message":
                for content in item.get("content", []):
                    if content.get("type") == "output_text":
                        return content.get("text", "").strip()

        return "मुझे अभी इसका जवाब नहीं मिल पाया।"

    except Exception as e:
        print("AI error:", e)
        return "AI से अभी connection नहीं हो पाया।"


# =========================================================
# COMMAND HANDLER
# =========================================================

def handle_command(command):

    if not command:
        return True

    # -----------------------------------------------------
    # STOP
    # -----------------------------------------------------

    if (
        command in ["stop", "exit", "quit", "बंद", "बंद करो"]
        or "jarvis band" in command
        or "jarvis band karo" in command
        or "बंद हो जाओ" in command
    ):
        speak("ठीक है, Jarvis बंद हो रही हूँ।")
        return False


    # -----------------------------------------------------
    # TIME
    # -----------------------------------------------------

    if (
        "समय" in command
        or "टाइम" in command
        or "time" in command
        or "samay" in command
    ):
        speak(phone_time())
        return True


    # -----------------------------------------------------
    # DAY
    # -----------------------------------------------------

    if (
        "दिन" in command
        or "दिन कौन" in command
        or "day" in command
        or "din" in command
    ):
        speak(phone_day())
        return True


    # -----------------------------------------------------
    # DATE
    # -----------------------------------------------------

    if (
        "तारीख" in command
        or "डेट" in command
        or "date" in command
        or "tarikh" in command
        or "aaj ki date" in command
        or "aaj ka date" in command
    ):
        speak(phone_date())
        return True


    # -----------------------------------------------------
    # WEATHER
    # -----------------------------------------------------

    if (
        "मौसम" in command
        or "mausam" in command
        or "weather" in command
    ):
        speak(live_weather("Patna"))
        return True


    # -----------------------------------------------------
    # YOUTUBE SEARCH
    # -----------------------------------------------------

    if (
        "youtube search" in command
        or "youtube par search" in command
        or "यूट्यूब पर सर्च" in command
    ):
        query = command

        for phrase in [
            "youtube search",
            "youtube par search",
            "यूट्यूब पर सर्च"
        ]:
            query = query.replace(phrase, "")

        query = query.strip()

        if query:
            youtube_search(query)
            speak("YouTube पर search खोल रही हूँ।")
        else:
            open_url("https://www.youtube.com")
            speak("YouTube खोल रही हूँ।")

        return True


    # -----------------------------------------------------
    # YOUTUBE
    # -----------------------------------------------------

    if (
        "youtube kholo" in command
        or "youtube खोलो" in command
        or "यूट्यूब खोलो" in command
        or command == "youtube"
        or command == "यूट्यूब"
    ):
        open_url("https://www.youtube.com")
        speak("YouTube खोल रही हूँ।")
        return True


    # -----------------------------------------------------
    # WHATSAPP
    # -----------------------------------------------------

    if (
        "whatsapp kholo" in command
        or "whatsapp खोलो" in command
        or "व्हाट्सएप खोलो" in command
        or command == "whatsapp"
        or command == "व्हाट्सएप"
    ):
        ok = open_app("com.whatsapp", ".Main")

        if ok:
            speak("WhatsApp खोल रही हूँ।")
        else:
            speak("WhatsApp खोल नहीं पाई।")

        return True


    # -----------------------------------------------------
    # INSTAGRAM
    # -----------------------------------------------------

    if (
        "instagram kholo" in command
        or "instagram खोलो" in command
        or "इंस्टाग्राम खोलो" in command
    ):
        open_url("https://www.instagram.com")
        speak("Instagram खोल रही हूँ।")
        return True


    # -----------------------------------------------------
    # FACEBOOK
    # -----------------------------------------------------

    if (
        "facebook kholo" in command
        or "facebook खोलो" in command
        or "फेसबुक खोलो" in command
    ):
        open_url("https://www.facebook.com")
        speak("Facebook खोल रही हूँ।")
        return True


    # -----------------------------------------------------
    # GOOGLE SEARCH
    # -----------------------------------------------------

    if (
        "google search" in command
        or "google par search" in command
        or "गूगल पर सर्च" in command
        or "search karo" in command
        or "सर्च करो" in command
    ):
        query = command

        for phrase in [
            "google search",
            "google par search",
            "गूगल पर सर्च",
            "search karo",
            "सर्च करो"
        ]:
            query = query.replace(phrase, "")

        query = query.strip()

        if query:
            google_search(query)
            speak("Google search खोल रही हूँ।")
        else:
            open_url("https://www.google.com")
            speak("Google खोल रही हूँ।")

        return True


    # -----------------------------------------------------
    # TIMER
    # -----------------------------------------------------

    timer_match = re.search(
        r"(\d+)\s*(second|seconds|sec|minute|minutes|min|hour|hours)",
        command
    )

    if timer_match:
        value = int(timer_match.group(1))
        unit = timer_match.group(2)

        if "hour" in unit:
            seconds = value * 3600
        elif "minute" in unit or "min" in unit:
            seconds = value * 60
        else:
            seconds = value

        if set_timer(seconds):
            speak(f"{value} {unit} का timer लगा रही हूँ।")
        else:
            speak("Timer लगाने में समस्या हुई।")

        return True


    # Hindi timer
    hindi_timer = re.search(
        r"(\d+)\s*(मिनट|मिनिट|सेकंड|घंटा)",
        command
    )

    if hindi_timer:
        value = int(hindi_timer.group(1))
        unit = hindi_timer.group(2)

        if unit == "घंटा":
            seconds = value * 3600
        elif unit in ["मिनट", "मिनिट"]:
            seconds = value * 60
        else:
            seconds = value

        if set_timer(seconds):
            speak(f"{value} का timer लगा रही हूँ।")
        else:
            speak("Timer लगाने में समस्या हुई।")

        return True


    # -----------------------------------------------------
    # WIFI
    # -----------------------------------------------------

    if (
        "wifi settings" in command
        or "wifi setting" in command
        or "वाईफाई सेटिंग" in command
        or "wifi kholo" in command
        or "वाईफाई खोलो" in command
    ):
        subprocess.run([
            "am",
            "start",
            "-a",
            "android.settings.WIFI_SETTINGS"
        ])

        speak("Wi-Fi settings खोल रही हूँ।")
        return True


    # -----------------------------------------------------
    # BLUETOOTH
    # -----------------------------------------------------

    if (
        "bluetooth settings" in command
        or "bluetooth setting" in command
        or "ब्लूटूथ सेटिंग" in command
        or "bluetooth kholo" in command
        or "ब्लूटूथ खोलो" in command
    ):
        subprocess.run([
            "am",
            "start",
            "-a",
            "android.settings.BLUETOOTH_SETTINGS"
        ])

        speak("Bluetooth settings खोल रही हूँ।")
        return True


    # -----------------------------------------------------
    # ANDROID SETTINGS
    # -----------------------------------------------------

    if (
        "settings kholo" in command
        or "settings खोलो" in command
        or "सेटिंग खोलो" in command
        or "फोन settings" in command
        or "फोन की setting" in command
    ):
        subprocess.run([
            "am",
            "start",
            "-a",
            "android.settings.SETTINGS"
        ])

        speak("Phone settings खोल रही हूँ।")
        return True


    # -----------------------------------------------------
    # CALL / DIAL
    # -----------------------------------------------------

    if (
        "call" in command
        or "कॉल" in command
        or "phone karo" in command
        or "फोन करो" in command
    ):
        number = extract_phone_number(command)

        if number:
            dial_number(number)
            speak(
                f"{number} का dialer खोल रही हूँ। "
                "आप चाहें तो वहाँ से call कर सकते हैं।"
            )
        else:
            speak(
                "मुझे valid mobile number नहीं मिला। "
                "किसी नंबर के साथ call command बोलें।"
            )

        return True


    # -----------------------------------------------------
    # TORCH
    # -----------------------------------------------------

    if (
        "torch" in command
        or "flashlight" in command
        or "टॉर्च" in command
    ):
        speak(
            "इस फोन के current Termux setup में "
            "Torch control available नहीं है।"
        )
        return True


    # -----------------------------------------------------
    # NOTIFICATION
    # -----------------------------------------------------

    if (
        "notification" in command
        or "नोटिफिकेशन" in command
        or "message आया" in command
        or "मैसेज आया" in command
    ):
        speak(
            "Notification पढ़ने के लिए Android Notification Access "
            "की permission और companion service चाहिए। "
            "मैं बिना permission के notifications नहीं पढ़ूँगी।"
        )
        return True


    # -----------------------------------------------------
    # AI FALLBACK
    # -----------------------------------------------------

    answer = ai_brain(command)
    speak(answer)

    return True


# =========================================================
# START
# =========================================================

if __name__ == "__main__":

    speak("नमस्ते, मैं जार्विस हूँ। मैं तैयार हूँ।")

    active = False

    while True:
        try:
            command = listen()

            if not command:
                continue

            low = command.lower().strip()

            wake_words = [
                "jarvis",
                "जार्विस",
                "जार्विस जी"
            ]

            stop_words = [
                "jarvis band",
                "jarvis bandh",
                "jarvis stop",
                "jarvis off",
                "जार्विस बंद",
                "जार्विस बन्द",
                "जार्विस स्टॉप",
                "जार्विस ऑफ"
            ]

            if any(x in low for x in stop_words):
                active = False
                speak("ठीक है, मैं शांत हूँ।")
                continue

            if not active:
                if any(x in low for x in wake_words):
                    active = True
                    speak("हाँ, बोलिए।")
                continue

            if not handle_command(command):
                break

        except KeyboardInterrupt:
            speak("Jarvis बंद।")
            break

        except Exception as e:
            print("MAIN ERROR:", e)
            continue
