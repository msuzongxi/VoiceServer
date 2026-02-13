#!/usr/bin/env python3
import os
import sys
import subprocess

def safe_stem(s: str) -> str:
    safe = []
    for ch in s:
        if ch.isalnum() or ch in ("-", "_", "."):
            safe.append(ch)
        else:
            safe.append("_")
    return "".join(safe)

def main():
    # Usage: extract_frames.py <video_path> <frames_dir> <uuid>
    if len(sys.argv) < 4:
        print("ERR missing args")
        return 2

    video_path = sys.argv[1]
    frames_dir = sys.argv[2]
    uuid = safe_stem(sys.argv[3])

    if not os.path.isfile(video_path):
        print("ERR video not found")
        return 3

    os.makedirs(frames_dir, exist_ok=True)

    out_pattern = os.path.join(frames_dir, f"{uuid}_%d.jpg")

    cmd = [
        "ffmpeg",
        "-y",
        "-i", video_path,
        "-ss", "0",
        "-t", "2",
        "-vf", "fps=2,scale=320:-1",
        "-start_number", "0",
        out_pattern
    ]

    p = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
    if p.returncode != 0:
        print("ERR ffmpeg")
        return 4

    print("OK")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
