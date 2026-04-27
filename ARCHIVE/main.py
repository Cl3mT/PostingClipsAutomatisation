import downloadClips as dl
import resizingClips as rs
import subtitlingClips as sub
import speedingClips as spc
import cleaning as cl
import describingClips as desc

import os

from pathlib import Path
from datetime import datetime

date_du_jour = datetime.now().strftime("%Y-%m-%d")
# Nettoyage des espaces vides potentiels dans le fichier texte
streamers_list = [line.strip() for line in Path("streamers2.txt").read_text().splitlines() if line.strip()]

if __name__ == "__main__":
    for streamer_name in streamers_list:
        print(f"\n{'='*40}")
        print(f"Début du traitement de {streamer_name}...")
        print(f"{'='*40}")
        
        # On télécharge les clips
        dl.downloadClip(streamer_name, 10)

        base_video_path = os.path.join("Clips_Twitch", f"Clips_{date_du_jour}", streamer_name)
        base_video_path_typed = Path(base_video_path)
        
        if not base_video_path_typed.exists():
            continue
            
        for clip in base_video_path_typed.glob("*.mp4"):
            print(f"\n🎬 Début du traitement du clip : {clip.name}...")

            #CREATING REPS NEEDED
            resized_video_path = os.path.join("Clips_Twitch_Resized", f"Clips_{date_du_jour}", streamer_name)
            os.makedirs(resized_video_path, exist_ok=True)

            subtitled_video_path = os.path.join("Clips_Twitch_Subtitled", f"Clips_{date_du_jour}", streamer_name)
            os.makedirs(subtitled_video_path, exist_ok=True)

            speeded_video_path = os.path.join("Clips_Twitch_Speeded", f"Clips_{date_du_jour}", streamer_name)
            os.makedirs(speeded_video_path, exist_ok=True)

            desc_file_path = os.path.join(speeded_video_path,clip.name).replace(".mp4",".txt")

            #RESIZING CLIP TO TIKTOK FORMAT
            
            try:
                rs.resizingClip(os.path.join(base_video_path, clip.name), resized_video_path)
                print(f"✅ Fin du traitement du clip : {clip.name}.")
                
            except ValueError as e:
                # Si l'erreur est notre ValueError ("Aucun visage détecté")
                print(f"⚠️ Clip ignoré : {e}")
                
            except Exception as e:
                # Sécurité supplémentaire au cas où une autre erreur inattendue surviendrait
                print(f"❌ Erreur inattendue sur le clip {clip.name} : {e}")

            #ADDING SUBTITLES
            texte_complet = sub.subtitlingClip(os.path.join(resized_video_path,clip.name),subtitled_video_path)

            #GENERATING THE DESCRIPTION
            if texte_complet:
                desc.generer_description(texte_complet, streamer_name, desc_file_path)

            #SPEEDING THE VIDEO UP
            spc.speedingClip(os.path.join(subtitled_video_path,clip.name),speeded_video_path)

            #CLEANING USELESS FILES
            dossiers_a_supprimer = [base_video_path,resized_video_path,subtitled_video_path]
            cl.cleaning(dossiers_a_supprimer,streamer_name)

            


            
        
        



            
            


        
        print(f"\nFin du traitement de {streamer_name}.")