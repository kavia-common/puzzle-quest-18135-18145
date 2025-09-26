#!/bin/bash
cd /home/kavia/workspace/code-generation/puzzle-quest-18135-18145/mobile_game_frontend
./gradlew lint
LINT_EXIT_CODE=$?
if [ $LINT_EXIT_CODE -ne 0 ]; then
   exit 1
fi

