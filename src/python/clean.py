import os
import shutil

def ask_delete(directory):
    while True:
        response = input(f"Voulez-vous supprimer le répertoire '{directory}' ? (y/n): ").strip().lower()
        if response == 'y':
            return True
        elif response == 'n':
            return False
        else:
            print("Réponse invalide. Veuillez répondre par 'y' ou 'n'.")

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
                    print(f"Répertoire '{dir_path}' supprimé avec succès.")
                except Exception as e:
                    print(f"Erreur lors de la suppression de '{dir_path}': {e}")
            else:
                print(f"Répertoire '{dir_path}' conservé.")
        else:
            print(f"Répertoire '{dir_path}' n'existe pas.")

if __name__ == "__main__":
    main()