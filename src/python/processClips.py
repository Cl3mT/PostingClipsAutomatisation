# --- Premiers imports ---
import config
import os

# --- Paramètre de configuration de ImageMagick ---
os.environ["IMAGEMAGICK_BINARY"] = config.get("IMAGEMAGICK_BINARY", r"C:\Program Files\ImageMagick-7.1.2-Q16-HDRI\magick.exe")

# --- Configuration du répertoire temporaire pour MoviePy ---
import tempfile
tempfile.tempdir = os.path.abspath("temp")
os.makedirs(tempfile.tempdir, exist_ok=True)

# --- Imports restants ---
import cv2
import whisper
import mediapipe as mp
from collections import Counter
from pathlib import Path
import moviepy.video.fx.all as vfx
from moviepy.editor import VideoFileClip, TextClip, CompositeVideoClip, clips_array

import describingClips as desc
import config

# --- Paramètres de configuration restants ---
WHISPER_MODEL = config.get("WHISPER_MODEL", "base")
WORDS_PER_LINE = config.get("WORDS_PER_LINE", 3, int)
SPEED_RATIO = config.get("SPEED_RATIO", 1.1, float)
THREADS_COUNT = config.get("THREADS", 4, int)

# ==============================================================================
# 1. FONCTIONS UTILITAIRES (Détection du visage et recadrage)
# ==============================================================================
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

    cam_x1 = max(0, cam_x1)
    cam_y1 = max(0, cam_y1)
    cam_x2 = min(width, cam_x2)
    cam_y2 = min(height, cam_y2)

    return int(cam_x1), int(cam_y1), int(cam_x2), int(cam_y2)


# ==============================================================================
# 2. FONCTION PRINCIPALE DE MONTAGE
# ==============================================================================
def process_final_clip(input_path, output_path, output_text_path):
    streamer_name = Path(input_path).parts[-2]
    print(f"\n🎬 DÉBUT DU MONTAGE COMPLET : {Path(input_path).name}")

    print(f"-> Extraction audio et analyse IA (Whisper {WHISPER_MODEL})...")
    video = VideoFileClip(input_path)
    
    audio_path = "temp/temp_audio.wav"
    video.audio.write_audiofile(audio_path, logger=None)

    model = whisper.load_model(WHISPER_MODEL) 
    result = model.transcribe(audio_path, language="fr", word_timestamps=True)
    texte_clip = result.get('text', '').strip()

    titre = desc.generer_description_et_titre(texte_clip, streamer_name, output_text_path)["Titre"]

    print("-> Recadrage de la caméra et du jeu...")
    TARGET_W, TARGET_H = 1080, 1920
    CAM_H, GAME_H = 640, 1280
    
    face_bbox = get_streamer_face_bbox(input_path) # Assure-toi que la fonction est bien définie au-dessus
    cam_x1, cam_y1, cam_x2, cam_y2 = get_camera_bbox(input_path, face_bbox)

    vid_w, vid_h = video.size
    cam_clip = video.crop(x1=cam_x1, y1=cam_y1, x2=cam_x2, y2=cam_y2).resize(newsize=(TARGET_W, CAM_H))
    
    game_target_ratio = TARGET_W / GAME_H
    crop_w = min(int(vid_h * game_target_ratio), vid_w)
    game_x1 = (vid_w - crop_w) // 2
    game_clip = video.crop(x1=game_x1, y1=0, x2=game_x1 + crop_w, y2=vid_h).resize(newsize=(TARGET_W, GAME_H))

    video_verticale = clips_array([[cam_clip], [game_clip]])
    video_verticale = video_verticale.set_audio(video.audio)

    print("-> Génération des sous-titres et du titre...")
    subs_clips = []

    if titre:
        texte_titre = f"  {titre.upper()}  "
        clip_titre = TextClip(texte_titre, fontsize=85, color='black', font='Impact', bg_color='white')
        y_position = CAM_H - (clip_titre.h // 2)
        clip_titre = (clip_titre.set_position(('center', y_position)).set_duration(video_verticale.duration))
        subs_clips.append(clip_titre)
    
    for segment in result.get('segments', []):
        mots_actuels = []
        debut_chunk = 0
        words = segment.get('words', [])
        
        for i, word_info in enumerate(words):
            mot = word_info['word'].strip().upper()
            if not mots_actuels:
                debut_chunk = word_info['start']
            mots_actuels.append(mot)
            
            # Utilisation du paramètre dynamique
            if len(mots_actuels) >= WORDS_PER_LINE or i == len(words) - 1:
                fin_chunk = word_info['end']
                if fin_chunk - debut_chunk < 0.1:
                    fin_chunk = debut_chunk + 0.1

                txt_clip = TextClip(
                    " ".join(mots_actuels), fontsize=100, color='white', 
                    font='Impact', stroke_color='black', stroke_width=6
                )
                txt_clip = (txt_clip.set_position(('center', 'center')).set_start(debut_chunk).set_end(fin_chunk))
                subs_clips.append(txt_clip)
                mots_actuels = []

    composite_finale = CompositeVideoClip([video_verticale] + subs_clips)

    print(f"-> Application de l'accélération (x{SPEED_RATIO})...")
    # Utilisation du paramètre dynamique
    composite_rapide = composite_finale.fx(vfx.speedx, SPEED_RATIO)

    print("-> Rendu de la vidéo en cours...")
    os.makedirs(os.path.dirname(output_path), exist_ok=True)

    composite_rapide.write_videofile(
        output_path, 
        codec="libx264", 
        audio_codec="aac", 
        fps=video.fps,
        preset="fast",
        threads=THREADS_COUNT # Utilisation du paramètre dynamique
    )

    if os.path.exists(audio_path):
        os.remove(audio_path)
        
    video.close()
    cam_clip.close()
    game_clip.close()
    video_verticale.close()
    composite_finale.close()
    composite_rapide.close()
    
    print(f"✅ Montage validé avec succès : {output_path}")