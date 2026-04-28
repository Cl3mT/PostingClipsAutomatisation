import os
import cv2
import unicodedata
import re
import numpy as np
import mediapipe as mp
from moviepy.editor import VideoFileClip, clips_array
from datetime import datetime

date_du_jour = datetime.now().strftime("%Y-%m-%d")

def clean_filename(filename):
    """Supprime les accents et remplace les caractères spéciaux."""
    filename = unicodedata.normalize('NFKD', filename).encode('ASCII', 'ignore').decode('utf-8')
    filename = re.sub(r'[^\w\s-]', '', filename).strip()
    return filename

def get_streamer_face_bbox(video_path, num_samples=20):
    """Trouve le visage le plus persistant pour ignorer les personnages du jeu."""
    cap = cv2.VideoCapture(video_path)
    total_frames = int(cap.get(cv2.CAP_PROP_FRAME_COUNT))
    step = max(1, total_frames // num_samples)
    
    use_mediapipe_solutions = hasattr(mp, "solutions") and hasattr(mp.solutions, "face_detection")
    face_detection = None
    cascade = None

    if use_mediapipe_solutions:
        mp_face_detection = mp.solutions.face_detection
        face_detection = mp_face_detection.FaceDetection(min_detection_confidence=0.5)
    else:
        cascade_path = os.path.join(cv2.data.haarcascades, "haarcascade_frontalface_default.xml")
        cascade = cv2.CascadeClassifier(cascade_path)
    
    detected_faces = []
    width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
    height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))

    for i in range(0, total_frames, step):
        cap.set(cv2.CAP_PROP_POS_FRAMES, i)
        ret, frame = cap.read()
        if not ret: continue

        if use_mediapipe_solutions:
            rgb_frame = cv2.cvtColor(frame, cv2.COLOR_BGR2RGB)
            results = face_detection.process(rgb_frame)

            if results.detections:
                for detection in results.detections:
                    bboxC = detection.location_data.relative_bounding_box
                    x = int(bboxC.xmin * width)
                    y = int(bboxC.ymin * height)
                    w = int(bboxC.width * width)
                    h = int(bboxC.height * height)
                    detected_faces.append((x, y, w, h))
        else:
            gray = cv2.cvtColor(frame, cv2.COLOR_BGR2GRAY)
            faces = cascade.detectMultiScale(gray, scaleFactor=1.1, minNeighbors=5, minSize=(40, 40))
            for (x, y, w, h) in faces:
                detected_faces.append((x, y, w, h))

    cap.release()
    if face_detection is not None:
        face_detection.close()

    if not detected_faces:
        raise ValueError("Aucun visage détecté dans la vidéo.")

    from collections import Counter
    rounded_faces = [(round(x, -1), round(y, -1), round(w, -1), round(h, -1)) for x, y, w, h in detected_faces]
    face_counts = Counter(rounded_faces)
    
    max_count = max(face_counts.values())
    stable_faces = [face for face, count in face_counts.items() if count >= (max_count * 0.6)]

    min_x = min([f[0] for f in stable_faces])
    min_y = min([f[1] for f in stable_faces])
    max_x = max([f[0] + f[2] for f in stable_faces])
    max_y = max([f[1] + f[3] for f in stable_faces])

    return int(min_x), int(min_y), int(max_x - min_x), int(max_y - min_y)


def get_camera_bbox(video_path, face_bbox):
    """Crée un cadre serré pour éviter de capturer le jeu autour de la webcam."""
    cap = cv2.VideoCapture(video_path)
    width = int(cap.get(cv2.CAP_PROP_FRAME_WIDTH))
    height = int(cap.get(cv2.CAP_PROP_FRAME_HEIGHT))
    cap.release()

    fx, fy, fw, fh = face_bbox
    
    TARGET_RATIO = 1080 / 640.0
    cam_w = int(fw * 2.6) 
    
    if cam_w > width:
        cam_w = width
        
    cam_h = int(cam_w / TARGET_RATIO)
    
    if cam_h < fh * 1.4:
        cam_h = int(fh * 1.4)
        cam_w = int(cam_h * TARGET_RATIO)
        
    face_center_x = fx + (fw // 2)
    face_center_y = fy + (fh // 2)
    
    cam_x1 = face_center_x - (cam_w // 2)
    cam_y1 = face_center_y - int(cam_h * 0.45) 
    cam_x2 = cam_x1 + cam_w
    cam_y2 = cam_y1 + cam_h

    if cam_x1 < 0:
        cam_x2 += abs(cam_x1)
        cam_x1 = 0
    if cam_x2 > width:
        cam_x1 -= (cam_x2 - width)
        cam_x2 = width
        
    if cam_y1 < 0:
        cam_y2 += abs(cam_y1)
        cam_y1 = 0
    if cam_y2 > height:
        cam_y1 -= (cam_y2 - height)
        cam_y2 = height

    # CORRECTION CRITIQUE 1 : Verrouillage absolu des coordonnées pour éviter le découpage négatif
    cam_x1 = max(0, cam_x1)
    cam_y1 = max(0, cam_y1)
    cam_x2 = min(width, cam_x2)
    cam_y2 = min(height, cam_y2)

    return int(cam_x1), int(cam_y1), int(cam_x2), int(cam_y2)

def create_tiktok_clip(input_path, output_path):
    TARGET_W = 1080
    TARGET_H = 1920
    CAM_H = 640    
    GAME_H = 1280  
    
    logger.info("1. Recherche du vrai visage du streamer...")
    face_bbox = get_streamer_face_bbox(input_path)
    
    logger.info("2. Création du cadre de caméra au ratio exact...")
    cam_x1, cam_y1, cam_x2, cam_y2 = get_camera_bbox(input_path, face_bbox)

    logger.info("3. Chargement de la vidéo...")
    video = VideoFileClip(input_path)
    vid_w, vid_h = video.size

    logger.info("4. Découpage et application...")
    
    cam_clip = video.crop(x1=cam_x1, y1=cam_y1, x2=cam_x2, y2=cam_y2)
    
    # CORRECTION CRITIQUE 2 : Utilisation de `newsize` pour forcer MoviePy à respecter les DEUX dimensions
    cam_clip = cam_clip.resize(newsize=(TARGET_W, CAM_H))

    game_target_ratio = TARGET_W / GAME_H
    
    crop_w = int(vid_h * game_target_ratio)
    crop_w = min(crop_w, vid_w) 
    
    game_x1 = (vid_w - crop_w) // 2
    game_x2 = game_x1 + crop_w
    
    game_clip = video.crop(x1=game_x1, y1=0, x2=game_x2, y2=vid_h)
    
    # CORRECTION CRITIQUE 2 (suite)
    game_clip = game_clip.resize(newsize=(TARGET_W, GAME_H))

    logger.info("5. Assemblage et rendu...")
    final_video = clips_array([[cam_clip], [game_clip]])
    final_video = final_video.set_audio(video.audio)

    final_video.write_videofile(
        output_path, 
        codec="libx264", 
        audio_codec="aac", 
        fps=video.fps,
        preset="fast",
        threads=4
    )
    logger.info("Terminé ! Vidéo sauvegardée sous : %s", output_path)
    
    clip_name = os.path.splitext(os.path.basename(input_path))[0]
    clip_name_clean = clean_filename(clip_name)
    
    output_path = os.path.join(output_path, f"{clip_name_clean}.mp4")
    create_tiktok_clip(input_path, output_path)