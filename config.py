import os

def load_settings(filepath="settings.txt"):
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

# On charge les paramètres en mémoire
SETTINGS = load_settings()

def get(key, default_value, cast_type=str):
    """Récupère une valeur et la convertit dans le bon type (int, float, str)."""
    val = SETTINGS.get(key)
    if val is None:
        return default_value
    try:
        return cast_type(val)
    except Exception:
        return default_value