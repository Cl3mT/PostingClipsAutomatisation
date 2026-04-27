import requests
import yt_dlp
import os
from datetime import datetime, timedelta, timezone
import config # <--- Import de la config

# Récupération dynamique depuis settings.txt
CLIENT_ID = config.get("TWITCH_CLIENT_ID", "")
CLIENT_SECRET = config.get("TWITCH_CLIENT_SECRET", "")
DAYS_FILTER = config.get("DAYS_FILTER", 7, int)

date_du_jour = datetime.now().strftime("%Y-%m-%d")

def obtenir_token_twitch():
    url = "https://id.twitch.tv/oauth2/token"
    parametres = {
        "client_id": CLIENT_ID,
        "client_secret": CLIENT_SECRET,
        "grant_type": "client_credentials"
    }
    reponse = requests.post(url, data=parametres)
    reponse.raise_for_status()
    return reponse.json()["access_token"]

def obtenir_id_streamer(nom_chaine, token):
    url = f"https://api.twitch.tv/helix/users?login={nom_chaine}"
    headers = {
        "Client-ID": CLIENT_ID,
        "Authorization": f"Bearer {token}"
    }
    reponse = requests.get(url, headers=headers)
    donnees = reponse.json()
    
    if not donnees.get("data"):
        print(f"❌ Impossible de trouver la chaîne : {nom_chaine}")
        return None
    return donnees["data"][0]["id"]

def recuperer_top_clips(broadcaster_id, token, limite=5):
    maintenant = datetime.now(timezone.utc)
    il_y_a_X_jours = maintenant - timedelta(days=DAYS_FILTER) 
    
    started_at = il_y_a_X_jours.strftime("%Y-%m-%dT%H:%M:%SZ")
    ended_at = maintenant.strftime("%Y-%m-%dT%H:%M:%SZ")
    
    url = f"https://api.twitch.tv/helix/clips?broadcaster_id={broadcaster_id}&first={limite}&started_at={started_at}&ended_at={ended_at}"

    headers = {
        "Client-ID": CLIENT_ID,
        "Authorization": f"Bearer {token}"
    }
    reponse = requests.get(url, headers=headers)
    donnees = reponse.json()
    
    liens_clips = []
    for clip in donnees.get("data", []):
        titre = clip['title']
        vues = clip['view_count']
        lien = clip['url']
        liens_clips.append(lien)
        print(f"🎬 Clip trouvé ({DAYS_FILTER} jours) : {titre} ({vues} vues)")
        
    if not liens_clips:
        print(f"🤷 Aucun clip trouvé pour cette chaîne dans les {DAYS_FILTER} derniers jours.")
        
    return liens_clips

def telecharger_clip(url):
    options = {
        'format': 'best[vcodec^=avc]/best[ext=mp4]/best', 
        'paths': {'home': DOSSIER_DESTINATION}, 
        'outtmpl': '%(title)s.%(ext)s', 
        'quiet': False
    }
    try:
        with yt_dlp.YoutubeDL(options) as ydl:
            ydl.download([url])
    except Exception as e:
        print(f"❌ Erreur lors du téléchargement : {e}")

def downloadClip(streamer_name, limite=5):
    print(f"--- Démarrage du téléchargeur (Filtre: {DAYS_FILTER} jours) ---")
    print("🔑 Connexion à l'API Twitch...")
    token_acces = obtenir_token_twitch()

    global DOSSIER_DESTINATION
    DOSSIER_DESTINATION = os.path.join("Clips_Twitch", f"Clips_{date_du_jour}", streamer_name)

    streamer_id = obtenir_id_streamer(streamer_name, token_acces)
    
    if streamer_id:
        print(f"\n🔍 Recherche des clips des {DAYS_FILTER} derniers jours pour {streamer_name}...")
        liens = recuperer_top_clips(streamer_id, token_acces, limite) 
        
        if liens:
            print("\n📥 Début des téléchargements...")
            for lien in liens:
                telecharger_clip(lien)
            print("\n✅ Terminé ! Les clips sont dans le dossier :", DOSSIER_DESTINATION)