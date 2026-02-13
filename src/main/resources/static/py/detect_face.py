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

def list_frames(frames_dir: str, uuid: str):
    pattern = os.path.join(frames_dir, f"{uuid}_*.jpg")
    return sorted(glob.glob(pattern))

def detect_with_facdetector_yn(img, model_path: str, score_th: float):
    """
    Preferred method: cv2.FaceDetectorYN (YuNet native API).
    Returns True if any face detected.
    """
    h, w = img.shape[:2]

    # Create detector (input size must match the image size)
    detector = cv2.FaceDetectorYN.create(
        model_path,
        "",                 # config (unused)
        (w, h),             # input size
        score_th,           # score threshold
        0.3,                # nms threshold (typical)
        5000                # topK
    )

    # returns (retval, faces); faces shape: [N, 15] or None
    ok, faces = detector.detect(img)
    if not ok or faces is None:
        return False

    # faces columns: x, y, w, h, ... score at index 14 (in many builds)
    # We'll just treat any returned row as a detection, but also filter tiny boxes:
    for f in faces:
        bw = float(f[2])
        bh = float(f[3])
        if bw >= 40 and bh >= 40:
            return True
    return False

def detect_with_dnn_fallback(img, model_path: str, score_th: float):
    """
    Fallback: run ONNX with cv2.dnn.
    Note: YuNet output format may vary by model/export; this is best-effort.
    Returns True if any face detected.
    """
    net = cv2.dnn.readNetFromONNX(model_path)

    # YuNet typically expects 320x320; we use that.
    inp_w, inp_h = 320, 320
    blob = cv2.dnn.blobFromImage(
        img,
        scalefactor=1.0 / 255.0,
        size=(inp_w, inp_h),
        mean=(0, 0, 0),
        swapRB=True,
        crop=False
    )
    net.setInput(blob)
    out = net.forward()

    # Best-effort parsing:
    # Many YuNet exports output [1, N, 15] where last value is score/confidence.
    # If shape differs, just return False rather than crash.
    try:
        # normalize to [N, M]
        if len(out.shape) == 3:
            preds = out[0]
        elif len(out.shape) == 2:
            preds = out
        else:
            return False

        for row in preds:
            # last field often score
            score = float(row[-1])
            if score >= score_th:
                # box often in first 4 fields (x, y, w, h) in input scale
                bw = float(row[2]) if len(row) >= 4 else 0.0
                bh = float(row[3]) if len(row) >= 4 else 0.0
                if bw >= 40 and bh >= 40:
                    return True
        return False
    except Exception:
        return False

def main():
    """
    Usage:
      detect_face_yunet.py <frames_dir> <uuid> <yunet_onnx_path> [score_threshold]

    Output:
      prints "1" if any face found, else "0"
    """
    if len(sys.argv) < 4:
        print("0")
        return 0

    frames_dir = sys.argv[1]
    uuid = safe_stem(sys.argv[2])
    model_path = sys.argv[3]
    score_th = float(sys.argv[4]) if len(sys.argv) >= 5 else 0.6  # try 0.5 if too strict

    if not os.path.isdir(frames_dir) or not os.path.exists(model_path):
        print("0")
        return 0

    frames = list_frames(frames_dir, uuid)
    if not frames:
        print("0")
        return 0

    has_facdetector = hasattr(cv2, "FaceDetectorYN") and hasattr(cv2.FaceDetectorYN, "create")

    for fp in frames:
        img = cv2.imread(fp)
        if img is None:
            continue

        found = False
        if has_facdetector:
            found = detect_with_facdetector_yn(img, model_path, score_th)
        else:
            found = detect_with_dnn_fallback(img, model_path, score_th)

        if found:
            print("1")
            return 0

    print("0")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
