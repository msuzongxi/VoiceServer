#!/usr/bin/env python3
import os
import sys
import wave

def main():
    """
    Usage:
      detect_speech.py <wav_path> [mode] [speech_ratio_threshold]

    Params:
      wav_path: must be 16-bit PCM WAV, mono, 16000 Hz (recommended)
      mode: 0-3 aggressiveness (default 2)  (3 = most strict, fewer false positives)
      speech_ratio_threshold: fraction of voiced frames required (default 0.08)

    Output:
      prints "1" if speech detected else "0"
    """
    if len(sys.argv) < 2:
        print("0")
        return 1

    wav_path = sys.argv[1]
    mode = int(sys.argv[2]) if len(sys.argv) >= 3 else 2
    ratio_th = float(sys.argv[3]) if len(sys.argv) >= 4 else 0.08

    if not os.path.exists(wav_path):
        print("0")
        return 1

    try:
        import webrtcvad
    except Exception:
        # dependency missing
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

        # WebRTC VAD requires 16-bit mono PCM
        if channels != 1 or sampwidth != 2 or rate not in (8000, 16000, 32000, 48000):
            # Not compatible audio format
            print("0")
            return 1

        vad = webrtcvad.Vad(mode)

        # 30ms frames are standard for VAD (10/20/30 allowed)
        frame_ms = 30
        frame_len = int(rate * frame_ms / 1000)         # samples per frame
        bytes_per_frame = frame_len * 2                 # 16-bit => 2 bytes/sample

        voiced = 0
        total = 0

        while True:
            frame = wf.readframes(frame_len)
            if len(frame) < bytes_per_frame:
                break
            total += 1
            if vad.is_speech(frame, rate):
                voiced += 1

        if total == 0:
            print("0")
            return 0

        speech_ratio = voiced / float(total)

        # Print 1 if speech ratio exceeds threshold
        if speech_ratio >= ratio_th:
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
