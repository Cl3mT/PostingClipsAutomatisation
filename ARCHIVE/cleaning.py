import shutil
import os

def cleaning(dossiers_a_supprimer,streamer_name):
    print(f"🧹 Nettoyage des dossiers temporaires pour {streamer_name}...")

    for dossier in dossiers_a_supprimer:
        if os.path.exists(dossier):
            try:
                 shutil.rmtree(dossier) # Détruit le dossier et tout son contenu
                 print(f"  - Dossier supprimé : {dossier}")
            except Exception as e:
                print(f"  ❌ Erreur lors de la suppression de {dossier} : {e}")

    print(f"\nFin du traitement de {streamer_name}.")