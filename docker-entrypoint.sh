#!/bin/sh
set -e

if [ -n "$FIRE_BASE_CONFIG_64" ]; then
  echo "Firebase 파일 만드는 중..."
  echo "$FIRE_BASE_CONFIG_64" | base64 -d > /firebase-adminsdk.json
  echo "Firebase 파일 완성!"
fi

java -jar /app.jar