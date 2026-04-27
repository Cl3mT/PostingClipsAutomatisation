import os
from dotenv import load_dotenv

# Charge les variables du fichier .env dans les variables d'environnement de l'OS
load_dotenv()

def load_settings(filepath=os.path.join(os.path.dirname(__file__), "../options/settings.txt")):
    settings = {}
    if not os.path.exists(filepath):
        print(f"⚠️ Fichier {filepath} introuvable. Utilisation des valeurs par défaut.")
        return settings
        
    with open(filepath, "r", encoding="utf-8") as f:
        for line in f:
            line = line.strip()
            # On ignore les lignes vides ou les commentaires
            if not line or line.startswith("#"):
                continue
            if ":" in line:
                # On coupe uniquement au premier ":" pour ne pas casser les chemins Windows (C:\...)
                key, value = line.split(":", 1)
                settings[key.strip()] = value.strip()
    return settings

# On charge les paramètres du settings.txt en mémoire
SETTINGS = load_settings()

def get(key, default_value, cast_type=str):
    """Récupère une valeur (depuis .env ou settings.txt) et la convertit."""
    
    # 1. On cherche d'abord dans les variables d'environnement (fichier .env)
    val = os.getenv(key)
    
    # 2. Si non trouvé dans le .env, on cherche dans le fichier settings.txt
    if val is None:
        val = SETTINGS.get(key)
        
    # 3. Si introuvable nulle part, on retourne la valeur par défaut
    if val is None:
        return default_value
        
    # On convertit dans le bon type (int, float, str...)
    try:
        return cast_type(val)
    except Exception:
        return default_value