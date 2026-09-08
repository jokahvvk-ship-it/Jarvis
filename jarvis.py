import pyttsx3
import speech_recognition as sr

engine = pyttsx3.init()
recognizer = sr.Recognizer()

def speak(text):
    print("Jarvis:", text)
    engine.say(text)
    engine.runAndWait()

speak("Hello, I am Jarvis")

print("Jarvis is ready.")
text = input("Jarvis को command दो: ").lower()

if "youtube" in text:
    speak("YouTube खोल रहा हूँ.")
else:
    speak("Command समझ गया.")
