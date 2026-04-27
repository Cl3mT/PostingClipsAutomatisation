import os
import google_auth_oauthlib.flow
import googleapiclient.discovery
import googleapiclient.errors
from googleapiclient.http import MediaFileUpload
from google.oauth2.credentials import Credentials
from google.auth.transport.requests import Request

# Définition des permissions requises
SCOPES = ["https://www.googleapis.com/auth/youtube.upload"]

def authentifier_youtube():
    """Gère l'authentification OAuth2 avec YouTube, en persistant les credentials."""
    creds = None
    token_file = "token.json"  # Fichier pour stocker les credentials
    
    # Vérifie si le fichier de token existe
    if os.path.exists(token_file):
        creds = Credentials.from_authorized_user_file(token_file, SCOPES)
    
    # Si les credentials n'existent pas ou sont expirés, on les récupère
    if not creds or not creds.valid:
        if creds and creds.expired and creds.refresh_token:
            creds.refresh(Request())
        else:
            os.environ["OAUTHLIB_INSECURE_TRANSPORT"] = "1"
            client_secrets_file = "client_secret.json"
            flow = google_auth_oauthlib.flow.InstalledAppFlow.from_client_secrets_file(
                client_secrets_file, SCOPES)
            creds = flow.run_local_server(port=0)
        
        # Sauvegarde les credentials pour les prochaines exécutions
        with open(token_file, 'w') as token:
            token.write(creds.to_json())
    
    # Construit le service YouTube
    youtube = googleapiclient.discovery.build(
        "youtube", "v3", credentials=creds)
    
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