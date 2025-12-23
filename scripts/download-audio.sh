#!/usr/bin/env bash
set -euo pipefail

# Script to download shortlisted audio assets into audio/original/
# Run locally: chmod +x scripts/download-audio.sh && ./scripts/download-audio.sh

mkdir -p audio/original

echo "Downloading Mixkit and Pixabay candidates to audio/original/"

# Mixkit files (should be directly downloadable)
curl -fL -o audio/original/success_02.wav "https://assets.mixkit.co/sfx/preview/mixkit-achievement-bell-600.wav" || echo "Failed to download success_02.wav"
curl -fL -o audio/original/countdown_tick.wav "https://assets.mixkit.co/sfx/preview/mixkit-soft-click-112.wav" || echo "Failed to download countdown_tick.wav"
curl -fL -o audio/original/level_up.wav "https://assets.mixkit.co/sfx/preview/mixkit-video-game-win-2016.wav" || echo "Failed to download level_up.wav"

# Pixabay files - some CDN downloads may require a Referer header
curl -fL -H "Referer: https://pixabay.com" -A "Mozilla/5.0" -o audio/original/success_01.mp3 "https://cdn.pixabay.com/download/audio/2021/08/04/audio_6297.mp3" || echo "Failed to download success_01.mp3"
curl -fL -H "Referer: https://pixabay.com" -A "Mozilla/5.0" -o audio/original/success_03.mp3 "https://cdn.pixabay.com/download/audio/2021/08/04/audio_6445.mp3" || echo "Failed to download success_03.mp3"
curl -fL -H "Referer: https://pixabay.com" -A "Mozilla/5.0" -o audio/original/error_gentle.mp3 "https://cdn.pixabay.com/download/audio/2022/03/15/audio_36058.mp3" || echo "Failed to download error_gentle.mp3"
curl -fL -H "Referer: https://pixabay.com" -A "Mozilla/5.0" -o audio/original/countdown_go.mp3 "https://cdn.pixabay.com/download/audio/2021/08/04/audio_6104.mp3" || echo "Failed to download countdown_go.mp3"
curl -fL -H "Referer: https://pixabay.com" -A "Mozilla/5.0" -o audio/original/time_warning.mp3 "https://cdn.pixabay.com/download/audio/2022/02/23/audio_33762.mp3" || echo "Failed to download time_warning.mp3"
curl -fL -H "Referer: https://pixabay.com" -A "Mozilla/5.0" -o audio/original/streak_continue.mp3 "https://cdn.pixabay.com/download/audio/2021/08/04/audio_951.mp3" || echo "Failed to download streak_continue.mp3"
curl -fL -H "Referer: https://pixabay.com" -A "Mozilla/5.0" -o audio/original/background_music.mp3 "https://cdn.pixabay.com/download/audio/2022/01/18/audio_11354.mp3" || echo "Failed to download background_music.mp3"

# Report
ls -lh audio/original || true

echo "Done. If any downloads failed, try running the script with network access or manually download the file from the source page and place it in audio/original/."
