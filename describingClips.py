from google import genai
import os
import json
import time
import config

# Paramètres IA
API_KEY = config.get("GEMINI_API_KEY", "")
MAX_TENTATIVES = config.get("MAX_RETRIES", 3, int)

def generer_description_et_titre(texte_clip, nom_streamer, output_text_path):
    print(f"Génération des métadonnées (Titre/Description) pour {nom_streamer}...")
    
    client = genai.Client(api_key=API_KEY)

    prompt = f"""
    Tu es un Community Manager expert sur TikTok et YouTube Shorts.
    Voici la transcription exacte d'un clip Twitch du streamer "{nom_streamer}" :
    
    "{texte_clip}"

    Ton objectif :
    1. Rédige un "Titre" de 3 à 5 mots maximum, très accrocheur, idéal pour être affiché sur la vidéo.
    2. Rédige une "Description" très courte, dynamique et virale (1 ou 2 phrases max) qui donne envie de regarder la vidéo ou de commenter.
    3. Ajoute 5 à 8 hashtags pertinents à la fin de la description (incluant #{nom_streamer}, #twitchfr, et des tags liés au gaming ou à l'humour).
    
    Réponds UNIQUEMENT au format JSON avec la structure exacte suivante :
    {{
        "Titre": "ton titre ici",
        "Description": "ta description avec hashtags ici"
    }}
    """
    
    for tentative in range(MAX_TENTATIVES):
        try:
            reponse = client.models.generate_content(
                model='gemini-2.5-flash',
                contents=prompt,
                config={"response_mime_type": "application/json"}
            )
            
            donnees_ia = json.loads(reponse.text)
            
            dossier_sortie = os.path.dirname(output_text_path)
            if dossier_sortie:
                os.makedirs(dossier_sortie, exist_ok=True)
                
            with open(output_text_path, "w", encoding="utf-8") as f:
                f.write(donnees_ia["Description"])
                
            print(f"✅ Métadonnées générées et description sauvegardée : {output_text_path}")
            return donnees_ia

        except Exception as e:
            erreur_str = str(e)
            if "503" in erreur_str or "UNAVAILABLE" in erreur_str or "429" in erreur_str:
                if tentative < MAX_TENTATIVES - 1:
                    print(f"⚠️ Serveur IA surchargé (tentative {tentative + 1}/{MAX_TENTATIVES}). Pause de 5 secondes...")
                    time.sleep(5)
                else:
                    print(f"❌ Échec après {MAX_TENTATIVES} tentatives.")
            else:
                print(f"❌ Erreur inattendue : {e}")
                break

    return {"Titre": "CLIP INCROYABLE", "Description": f"Regardez ça ! #{nom_streamer} #twitchfr"}