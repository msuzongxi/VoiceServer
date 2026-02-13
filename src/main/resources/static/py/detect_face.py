#!/usr/bin/env python3
import os
import sys
import glob
import cv2

def safe_stem(s: str) -> str:
    safe = []
    for ch in s:
        if ch.isalnum() or ch in ("-", "_", "."):
            safe.append(ch)
        else:
            safe.append("_")
    return "".join(safe)

def main():
    # Usage: detect_face.py <frames_dir> <uuid>
    if len(sys.argv) < 3:
        print("0")
        return 0

    frames_dir = sys.argv[1]
    uuid = safe_stem(sys.argv[2])

    pattern = os.path.join(frames_dir, f"{uuid}_*.jpg")
    frames = sorted(glob.glob(pattern))
    if not frames:
        print("0")
        return 0

    CASCADE_PATH = os.path.join(
    os.path.dirname(__file__),
    "..", "cascades", "haarcascade_frontalface_default.xml")

	cascade = cv2.CascadeClassifier(CASCADE_PATH)
	if cascade.empty():
    	raise RuntimeError("Failed to load cascade xml: " + CASCADE_PATH)

    for fp in frames:
        img = cv2.imread(fp)
        if img is None:
            continue
        gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
        faces = cascade.detectMultiScale(gray, 1.1, 5, minSize=(40, 40))
        if len(faces) > 0:
            print("1")
            return 0

    print("0")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
