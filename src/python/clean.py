import os
import shutil
from logger_config import logger

def ask_delete(directory):
    while True:
        response = input(f"Voulez-vous supprimer le répertoire '{directory}' ? (y/n): ").strip().lower()
        if response == 'y':
            return True
        elif response == 'n':
            return False
        else:
            logger.warning("Réponse invalide. Veuillez répondre par 'y' ou 'n'.")

def main():
    project_root = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
    directories = [
        os.path.join(project_root, "temp"),
        os.path.join(project_root, "Clips_Tiktok_Prets"),
        os.path.join(project_root, "Clips_Twitch")
    ]
    
    for dir_path in directories:
        if os.path.exists(dir_path):
            if ask_delete(dir_path):
                try:
                    shutil.rmtree(dir_path)
                    logger.info("Répertoire '%s' supprimé avec succès.", dir_path)
                except Exception as e:
                    logger.error("Erreur lors de la suppression de '%s': %s", dir_path, e, exc_info=True)
            else:
                logger.info("Répertoire '%s' conservé.", dir_path)
        else:
            logger.warning("Répertoire '%s' n'existe pas.", dir_path)

if __name__ == "__main__":
    main()