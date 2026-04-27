import os
from moviepy.editor import VideoFileClip
import moviepy.video.fx.all as vfx

ACC_RATIO = 1.1

def speedingClip(input_path, output_path):
    """
    Prend une vidéo en entrée, l'accélère de 10% (vidéo et audio),
    et sauvegarde la nouvelle vidéo.
    """
    print(f"Ouverture de la vidéo : {input_path}")
    
    # Charger la vidéo
    video = VideoFileClip(input_path)
    
    # --- APPLICATION DE L'EFFET ---
    print("Application de l'accélération (10%)...")
    # vfx.speedx avec un facteur de 1.1 accélère la vidéo de 10%
    # Il traite automatiquement la piste audio pour qu'elle reste synchronisée.
    video_acceleree = video.fx(vfx.speedx, ACC_RATIO)
    
    # Sécurité : Créer les dossiers de destination s'ils n'existent pas
    dossier_sortie = os.path.dirname(output_path)
    if dossier_sortie:
        os.makedirs(dossier_sortie, exist_ok=True)
        
    # Exporter la vidéo
    print("Exportation de la nouvelle vidéo...")
    video_acceleree.write_videofile(
        output_path, 
        codec="libx264", 
        audio_codec="aac", 
        fps=video.fps, # On garde le nombre d'images par seconde d'origine
        threads=4
    )
    
    # Bonne pratique : fermer les clips pour libérer la mémoire RAM
    # et éviter les erreurs "Fichier utilisé par un autre processus" sous Windows
    video.close()
    video_acceleree.close()
    
    print(f"✅ Vidéo accélérée générée avec succès : {output_path}")