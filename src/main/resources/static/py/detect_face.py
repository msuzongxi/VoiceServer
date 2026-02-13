#!/usr/bin/env python3
import os
import sys
import glob
import cv2

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
      detect_face_yunet.py <frames_dir> <uuid> <yunet_onnx_path> [score_th]

    Output:
      prints "1" if any face detected, else "0"
    """
    if len(sys.argv) < 4:
        print("0")
        return 0

    frames_dir = sys.argv[1]
    uuid = safe_stem(sys.argv[2])
    model_path = sys.argv[3]
    score_th = float(sys.argv[4]) if len(sys.argv) >= 5 else 0.5

    if not os.path.isdir(frames_dir):
        print("0")
        return 0

    if not os.path.exists(model_path):
        print("0")
        return 0

    if not (hasattr(cv2, "FaceDetectorYN") and hasattr(cv2.FaceDetectorYN, "create")):
        # Your cv2 build doesn't include YuNet API
        print("0")
        return 0

    frames = sorted(glob.glob(os.path.join(frames_dir, f"{uuid}_*.jpg")))
    if not frames:
        print("0")
        return 0

    # IMPORTANT: fixed input size to avoid OpenCV 4.5.4 DNN shape mismatch bugs
    inp_w, inp_h = 320, 320

    detector = cv2.FaceDetectorYN.create(
        model_path,
        "",                 # config (unused)
        (inp_w, inp_h),      # fixed input size
        score_th,            # score threshold
        0.3,                 # nms threshold
        5000                 # topK
    )

    for fp in frames:
        img = cv2.imread(fp)
        if img is None:
            continue

        # IMPORTANT: resize to match detector input size
        resized = cv2.resize(img, (inp_w, inp_h))

        try:
            ok, faces = detector.detect(resized)
        except Exception:
            # If something goes wrong for this frame, just skip it
            continue

        if ok and faces is not None and len(faces) > 0:
            # Filter tiny detections to reduce false positives
            for f in faces:
                bw = float(f[2])
                bh = float(f[3])
                if bw >= 40 and bh >= 40:
                    print("1")
                    return 0

    print("0")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
