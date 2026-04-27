import os
import google_auth_oauthlib.flow
import googleapiclient.discovery
import googleapiclient.errors
from googleapiclient.http import MediaFileUpload

# Définition des permissions requises
SCOPES = ["https://www.googleapis.com/auth/youtube.upload"]

def authentifier_youtube():
    """Gère l'authentification OAuth2 avec YouTube."""
    os.environ["OAUTHLIB_INSECURE_TRANSPORT"] = "1"
    
    client_secrets_file = "client_secret.json" # Assurez-vous que ce fichier est à la racine

    # Récupère les identifiants via le navigateur à la première exécution
    flow = google_auth_oauthlib.flow.InstalledAppFlow.from_client_secrets_file(
        client_secrets_file, SCOPES)
    
    # Cela va ouvrir une page web pour vous demander d'autoriser l'application
    credentials = flow.run_local_server(port=0)
    
    # Construit le service YouTube
    youtube = googleapiclient.discovery.build(
        "youtube", "v3", credentials=credentials)
    
    return youtube

def upload_short(youtube, video_path, titre, description):
    """Envoie la vidéo sur YouTube en tant que Short."""
    print(f"⬆️ Début de l'upload YouTube pour : {titre}")
    
    # Pour s'assurer que c'est un Short, on ajoute le hashtag dans le titre ou la description
    titre_complet = f"{titre[:80]} #Shorts" # YouTube limite les titres à 100 caractères
    
    request_body = {
        "snippet": {
            "categoryId": "20", # 20 = Gaming
            "title": titre_complet,
            "description": description,
            "tags": ["shorts", "twitchfr", "gaming", "clip"]
        },
        "status": {
            "privacyStatus": "public", # Vous pouvez mettre "private" pour tester au début
            "selfDeclaredMadeForKids": False
        }
    }

    media_file = MediaFileUpload(video_path, chunksize=-1, resumable=True)

    request = youtube.videos().insert(
        part="snippet,status",
        body=request_body,
        media_body=media_file
    )

    try:
        response = request.execute()
        print(f"✅ Vidéo uploadée avec succès ! URL : https://youtube.com/shorts/{response['id']}")
        return response['id']
    except googleapiclient.errors.HttpError as e:
        print(f"❌ Erreur lors de l'upload : {e}")
        return None