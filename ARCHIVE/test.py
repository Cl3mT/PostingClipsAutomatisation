import os
import subtitlingClips as sub

video_path = os.path.join("Clips_Twitch_Resized","Clips_2026-04-09","zerator","ZeratoR apprend quil est chevalier.mp4")
output_path = os.path.join("Clips_Twitch_Subtitled","Clips_2026-04-09","zerator","ZeratoR apprend quil est chevalier.mp4")

dossier_sortie = os.path.dirname(output_path)
if dossier_sortie:
    os.makedirs(dossier_sortie, exist_ok=True)

sub.subtitlingClip(video_path,output_path)
