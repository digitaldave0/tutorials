import os 
import subprocess
import speech_recognition as sr
from pytube import YouTube
from reportlab.lib.pagesizes import letter
from reportlab.pdfgen import canvas
from gtts import gTTS

def download_youtube_video(url, output_path):
    yt = YouTube(url)
    stream = yt.stream.filter(file_extension='mp4').first()
    stream.download(output_path)

def extract_audio(video_path, output_path):
    command = f"ffmpeg -i {video_path} -vn {output_path}"
    subprocess.call(command, shell=True)

def transcribe_audio(audio_path):
    recognizer = sr.Recognizer()
    with sr.AudioFile(audio_path) as source:
        audio_text = recognizer.recognize_google(audio=source)
        return audio_text

def create_pdf(text, output_path):
    c = canvas.Canvas(output_path, pagesize=letter)
    c.drawString(100,750,text)
    c.save()

def create_audio_file(text, output_path):
    tts = gTTS(text, lang='en')
    tts.save(output_path)

if __name__ == "__main__":
    youtube_url = "https://www.youtube.com/watch?v=np9jfnwnO2c"
    video_output_path = "~/Projects/test.mp4"
    audio_output_path = "~/Projects/test.wav"
    pdf_output_path = "~/Projects/test.pdf"
    audio_file_output_path = "~/Projects/test.mp3"

download_youtube_video(youtube_url,video_output_path)
extract_audio(video_output_path,audio_output_path)
transcribe_audio(audio_output_path)
##create_pdf(, pdf_output_path)
##create_audio_file(transcribe_audio, audio_file_output_path)

