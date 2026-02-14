#!/usr/bin/env python3
import os
import sys
import subprocess

def safe_stem(s: str) -> str:
    out = []
    for ch in s:
        if ch.isalnum() or ch in ("-", "_", "."):
            out.append(ch)
        else:
            out.append("_")
    return "".join(out)

def main():
    """
    Usage:
      extract_audio.py <video_path> <audio_output_dir> <uuid>

    Output:
      creates <audio_output_dir>/<uuid>.wav
    """

    if len(sys.argv) < 4:
        print("Usage: extract_audio.py <video_path> <audio_output_dir> <uuid>")
        return 1

    video_path = sys.argv[1]
    audio_dir = sys.argv[2]
    uuid = safe_stem(sys.argv[3])

    if not os.path.exists(video_path):
        print("Video not found")
        return 1

    os.makedirs(audio_dir, exist_ok=True)

    output_path = os.path.join(audio_dir, f"{uuid}.wav")

    cmd = [
        "ffmpeg",
        "-y",                  # overwrite
        "-i", video_path,
        "-vn",                 # no video
        "-ac", "1",            # mono
        "-ar", "16000",        # 16kHz
        "-c:a", "pcm_s16le",   # 16-bit PCM
        output_path
    ]

    try:
        p = subprocess.run(
            cmd,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True
        )

        if p.returncode != 0:
            print("FFmpeg failed")
            return 1

        if not os.path.exists(output_path):
            print("Audio not created")
            return 1

        print(output_path)
        return 0

    except Exception as e:
        print("Exception:", str(e))
        return 1

if __name__ == "__main__":
    raise SystemExit(main())
