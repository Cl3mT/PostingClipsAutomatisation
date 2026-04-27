import downloadClips as dl
import processClips as pc
import shutil 
import os
from pathlib import Path
from datetime import datetime

import config # <--- Import de la config

# Récupération de la limite de téléchargement depuis les paramètres
CLIPS_LIMIT = config.get("CLIP_LIMIT", 10, int)

date_du_jour = datetime.now().strftime("%Y-%m-%d")

# Lecture du fichier de streamers
# Tu peux changer le nom en streamers.txt si c'est celui que tu utilises désormais.
streamers_list = [line.strip() for line in Path(os.path.join(os.path.dirname(__file__), "../options/streamers.txt")).read_text().splitlines() if line.strip()]

if __name__ == "__main__":
    for streamer_name in streamers_list:
        print(f"\n{'='*40}")
        print(f"Début du traitement de {streamer_name}...")
        print(f"{'='*40}")
        
        # 1. On télécharge les clips avec la limite définie dans settings.txt
        dl.downloadClip(streamer_name, limite=CLIPS_LIMIT)

        # 2. On vérifie que le dossier source a bien été créé
        base_video_path = os.path.join("Clips_Twitch", f"Clips_{date_du_jour}", streamer_name)
        base_video_path_typed = Path(base_video_path)
        
        if not base_video_path_typed.exists():
            print(f"Aucun clip trouvé pour {streamer_name} aujourd'hui.")
            continue
            
        # 3. Traitement de chaque clip
        for clip in base_video_path_typed.glob("*.mp4"):
            print(f"\n🎬 Préparation du clip : {clip.name}...")

            dossier_final = os.path.join("Clips_Tiktok_Prets", f"Clips_{date_du_jour}", streamer_name)
            chemin_video_finale = os.path.join(dossier_final, clip.name)
            chemin_fichier_texte = chemin_video_finale.replace(".mp4", ".txt")

            try:
                pc.process_final_clip(
                    input_path=os.path.join(base_video_path, clip.name),
                    output_path=chemin_video_finale,
                    output_text_path=chemin_fichier_texte
                )
            except ValueError as e:
                print(f"⚠️ Clip ignoré : {e}")
            except Exception as e:
                print(f"❌ Erreur inattendue sur le clip {clip.name} : {e}")

        # 4. Nettoyage du dossier source (téléchargements initiaux)
        print(f"\n🧹 Nettoyage du dossier source pour {streamer_name}...")
        if os.path.exists(base_video_path):
            try:
                shutil.rmtree(base_video_path)
                print(f"  - Dossier source supprimé : {base_video_path}")
            except Exception as e:
                print(f"  ❌ Erreur lors de la suppression de {base_video_path} : {e}")

        print(f"\nFin du traitement de {streamer_name}.")