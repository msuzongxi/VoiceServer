#!/usr/bin/env python3
import os
import sys
import wave
import struct
import math

def frame_rms_int16(frame_bytes: bytes) -> float:
    """Compute RMS of 16-bit PCM mono frame."""
    if not frame_bytes:
        return 0.0
    count = len(frame_bytes) // 2
    samples = struct.unpack("<" + "h"*count, frame_bytes)
    # RMS
    s2 = 0.0
    for x in samples:
        s2 += float(x) * float(x)
    return math.sqrt(s2 / max(1, count))

def main():
    """
    Usage:
      detect_speech.py <wav_path> [mode] [speech_ratio_threshold] [min_rms] [min_consecutive]

    Defaults tuned to be LESS permissive than before:
      mode=2
      speech_ratio_threshold=0.06
      min_rms=200      (energy gate; tune if needed)
      min_consecutive=3  (>= 3 consecutive 30ms speech frames => 90ms)
    """
    if len(sys.argv) < 2:
        print("0")
        return 1

    wav_path = sys.argv[1]
    mode = int(sys.argv[2]) if len(sys.argv) >= 3 else 2
    ratio_th = float(sys.argv[3]) if len(sys.argv) >= 4 else 0.06
    min_rms = float(sys.argv[4]) if len(sys.argv) >= 5 else 200.0
    min_consec = int(sys.argv[5]) if len(sys.argv) >= 6 else 3

    if not os.path.exists(wav_path):
        print("0")
        return 1

    try:
        import webrtcvad
    except Exception:
        print("0")
        return 1

    try:
        wf = wave.open(wav_path, "rb")
    except Exception:
        print("0")
        return 1

    try:
        channels = wf.getnchannels()
        rate = wf.getframerate()
        sampwidth = wf.getsampwidth()

        if channels != 1 or sampwidth != 2 or rate not in (8000, 16000, 32000, 48000):
            print("0")
            return 1

        vad = webrtcvad.Vad(mode)

        frame_ms = 30
        frame_len = int(rate * frame_ms / 1000)
        bytes_per_frame = frame_len * 2

        voiced = 0
        total = 0

        consec = 0
        max_consec = 0

        while True:
            frame = wf.readframes(frame_len)
            if len(frame) < bytes_per_frame:
                break

            total += 1

            # Energy gate first: ignore very quiet frames
            rms = frame_rms_int16(frame)
            if rms < min_rms:
                consec = 0
                continue

            # VAD decision
            if vad.is_speech(frame, rate):
                voiced += 1
                consec += 1
                if consec > max_consec:
                    max_consec = consec
            else:
                consec = 0

        if total == 0:
            print("0")
            return 0

        speech_ratio = voiced / float(total)

        # Decision: require both ratio and some consecutive speech
        if (speech_ratio >= ratio_th) and (max_consec >= min_consec):
            print("1")
        else:
            print("0")

        return 0

    finally:
        try:
            wf.close()
        except Exception:
            pass

if __name__ == "__main__":
    raise SystemExit(main())
