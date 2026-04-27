import os
import whisper
from pathlib import Path

import describingClips as desc

# --- Ton chemin vers ImageMagick ---
os.environ["IMAGEMAGICK_BINARY"] = r"C:\Program Files\ImageMagick-7.1.2-Q16-HDRI\magick.exe"

from moviepy.editor import VideoFileClip, TextClip, CompositeVideoClip

def subtitlingClip(input_path, output_path):
    streamer_name = Path(input_path).parts[-2]
    output_text_path = output_path.replace(".mp4",".txt")

    print(f"Ouverture de la vidéo : {input_path}")
    video = VideoFileClip(input_path)
    
    audio_path = "temp_audio.wav"
    print("Extraction de l'audio...")
    video.audio.write_audiofile(audio_path, logger=None)

    print("Transcription de l'audio avec Whisper...")
    model = whisper.load_model("base") 
    
    # ON RÉACTIVE LES TIMESTAMPS PAR MOT
    result = model.transcribe(audio_path, language="fr", word_timestamps=True)

    # ON UTILISE LE TEXTE POUR EN DEDUIRE LE TITRE ET LA DESCRIPTION DE LA VIDEO
    texte_clip = result.get('text', '').strip()
    titre = desc.generer_description_et_titre(texte_clip, streamer_name, output_text_path)["Titre"]

    print("Génération des sous-titres synchronisés...")
    subs_clips = []

    # =========================================================
    # --- AJOUT : CRÉATION DU CLIP TITRE SUR FOND BLANC ---
    # =========================================================
    if titre:
        print(f"Création du titre fixe : {titre}")
        # On ajoute des espaces au début et à la fin pour faire "respirer" 
        # le texte dans son fond blanc (effet de padding)
        texte_titre = f"  {titre.upper()}  "
        
        clip_titre = TextClip(
            texte_titre,
            fontsize=85,
            color='black',          # Texte en noir
            font='Impact',
            bg_color='white'        # Fond blanc rectangulaire
        )
        
        # POSITIONNEMENT : À cheval sur la séparation
        # On place le haut du titre à 640 moins la moitié de sa hauteur
        y_position = 640 - (clip_titre.h // 2)
        
        clip_titre = (clip_titre
                      .set_position(('center', y_position))
                      .set_duration(video.duration))
                      
        # On ajoute le titre à la liste des calques
        subs_clips.append(clip_titre)
    # =========================================================
    
    # --- LOGIQUE : GROUPER PAR 3-4 MOTS ---
    mots_par_ligne = 3
    
    for segment in result.get('segments', []):
        mots_actuels = []
        debut_chunk = 0
        
        words = segment.get('words', [])
        for i, word_info in enumerate(words):
            mot = word_info['word'].strip().upper()
            
            if len(mots_actuels) == 0:
                debut_chunk = word_info['start']
                
            mots_actuels.append(mot)
            
            if len(mots_actuels) >= mots_par_ligne or i == len(words) - 1:
                fin_chunk = word_info['end']
                texte_final = " ".join(mots_actuels) 
                
                if fin_chunk - debut_chunk < 0.1:
                    fin_chunk = debut_chunk + 0.1

                txt_clip = TextClip(
                    texte_final,
                    fontsize=100,           
                    color='white',          
                    font='Impact',
                    stroke_color='black',   
                    stroke_width=6          
                )

                txt_clip = (txt_clip
                            .set_position(('center', 'center'))
                            .set_start(debut_chunk)
                            .set_end(fin_chunk))

                subs_clips.append(txt_clip)
                mots_actuels = []

    print("Assemblage final de la vidéo...")
    final_video = CompositeVideoClip([video] + subs_clips)

    final_video.write_videofile(
        output_path, 
        codec="libx264", 
        audio_codec="aac", 
        fps=video.fps,
        threads=4
    )

    if os.path.exists(audio_path):
        os.remove(audio_path)
        
    print(f"✅ Vidéo sous-titrée générée avec succès : {output_path}")