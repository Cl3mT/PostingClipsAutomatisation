import shutil
import os
from logger_config import logger

def cleaning(dossiers_a_supprimer,streamer_name):
    logger.info("🧹 Nettoyage des dossiers temporaires pour %s...", streamer_name)

    for dossier in dossiers_a_supprimer:
        if os.path.exists(dossier):
            try:
                 shutil.rmtree(dossier) # Détruit le dossier et tout son contenu
                 logger.info("  - Dossier supprimé : %s", dossier)
            except Exception as e:
                logger.error("  ❌ Erreur lors de la suppression de %s : %s", dossier, e, exc_info=True)

    logger.info("Fin du traitement de %s.", streamer_name)