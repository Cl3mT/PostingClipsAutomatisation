import logger_config
import downloadClips as dl
import processClips as pc
import shutil
import os
import uploadYoutube as yt_upload
from pathlib import Path
from datetime import datetime, timedelta, timezone

import config

logger = logger_config.logger

CLIPS_LIMIT = config.get("CLIP_LIMIT", 10, int)
date_du_jour = datetime.now().strftime("%Y-%m-%d")

streamers_list = [line.strip() for line in Path(os.path.join(os.path.dirname(__file__), "../options/streamers.txt")).read_text().splitlines() if line.strip()]

if __name__ == "__main__":
    logger.info("🔑 Authentification YouTube en cours...")
    youtube_service = yt_upload.authentifier_youtube()

    # --- INITIALISATION DE LA PLANIFICATION ---
    # On prévoit de commencer à publier dès demain
    date_planification = datetime.now(timezone.utc) + timedelta(days=1)
    
    # Heure stratégique pour le buzz (ex: 18h00 UTC)
    date_planification = date_planification.replace(hour=18, minute=0, second=0, microsecond=0)
    # ------------------------------------------

    for streamer_name in streamers_list:
        logger.info("")
        logger.info("%s", "=" * 40)
        logger.info("Début du traitement de %s...", streamer_name)
        logger.info("%s", "=" * 40)
        
        dl.downloadClip(streamer_name, limite=CLIPS_LIMIT)

        base_video_path = os.path.join("Clips_Twitch", f"Clips_{date_du_jour}", streamer_name)
        base_video_path_typed = Path(base_video_path)
        
        if not base_video_path_typed.exists():
            logger.warning("Aucun clip trouvé pour %s aujourd'hui.", streamer_name)
            continue
            
        for clip in base_video_path_typed.glob("*.mp4"):
            logger.info("🎬 Préparation du clip : %s...", clip.name)

            dossier_final = os.path.join("Clips_Tiktok_Prets", f"Clips_{date_du_jour}", streamer_name)
            chemin_video_finale = os.path.join(dossier_final, clip.name)
            chemin_fichier_texte = chemin_video_finale.replace(".mp4", ".txt")

            try:
                pc.process_final_clip(
                    input_path=os.path.join(base_video_path, clip.name),
                    output_path=chemin_video_finale,
                    output_text_path=chemin_fichier_texte
                )

                logger.info("🚀 Préparation de la publication sur YouTube Shorts...")
                
                description_clip = ""
                if os.path.exists(chemin_fichier_texte):
                    with open(chemin_fichier_texte, "r", encoding="utf-8") as f:
                        description_clip = f.read()
                
                titre_clip = os.path.splitext(clip.name)[0]
                
                # --- FORMATAGE DE LA DATE POUR YOUTUBE (ISO 8601) ---
                publish_at_iso = date_planification.strftime("%Y-%m-%dT%H:%M:%SZ")
                
                yt_upload.upload_short(
                    youtube=youtube_service,
                    video_path=chemin_video_finale,
                    titre=titre_clip,
                    description=description_clip,
                    publish_at=publish_at_iso # <-- On transmet la date ici
                )

                # --- CALCUL DU PROCHAIN CRÉNEAU POUR LE BUZZ ---
                # Option 1 : Si tu as peu de clips (ex: 30), on espace de 1 jour (1 vidéo par jour à 18h)
                # date_planification += timedelta(days=1) 
                
                # Option 2 (Recommandée) : Si tu as ta liste de 20 streamers x 5 clips (100 clips au total).
                # Pour écouler 100 clips en 30 jours, il faut poster ~3 vidéos par jour.
                # On ajoute donc 8 heures entre chaque publication (ex: 10h, 18h, 02h).
                date_planification += timedelta(hours=8)

            except ValueError as e:
                logger.warning("⚠️ Clip ignoré : %s", e)
            except Exception as e:
                logger.error("❌ Erreur inattendue sur le clip %s : %s", clip.name, e, exc_info=True)

        # Nettoyage
        logger.info("🧹 Nettoyage du dossier source pour %s...", streamer_name)
        if os.path.exists(base_video_path):
            try:
                shutil.rmtree(base_video_path)
                logger.info("  - Dossier source supprimé : %s", base_video_path)
            except Exception as e:
                logger.error("  ❌ Erreur lors de la suppression de %s : %s", base_video_path, e, exc_info=True)

    logger.info("🎉 Tous les clips ont été traités et planifiés sur YouTube !")