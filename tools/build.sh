#!/bin/bash
set -e
W=/home/claude/w
V=${1:-1.5.11}
VU=$(echo $V | tr . _)
B=$W/b$VU
cd $W
rm -rf $B; cp -r $W/jar1510 $B
CP=$(cat $W/rcp.txt)
rm -rf $W/out; mkdir -p $W/out
S=$W/src/dev/pete/frierenarcana
javac -nowarn -implicit:none -proc:none -encoding UTF-8 -d $W/out -cp "$CP" \
  $S/client/BreakTimeline.java $S/client/BreakerFx.java $S/client/SpellCircleFx.java $S/client/CinemaDirector.java \
  $S/client/CinemaOverlay.java $S/client/BarrierLook.java $S/client/BreakerSound.java $S/client/ClientRainHold.java \
  $S/client/CinemaPost.java $S/client/BreakerAudio.java $S/client/FernBarrageFx.java $S/client/CinemaFov.java $S/RainHold.java $S/BarrageFern.java $S/StaffFlight.java $EXTRA_SRC > $W/javac.log 2>&1 || { grep -v JAVA_TOOL $W/javac.log; echo "COMPILE FAILED"; exit 1; }
grep -v JAVA_TOOL $W/javac.log | grep -v "^Note:" || true
cp -r $W/out/* $B/
java -Dlife=34.0 -DbeamLife=15.35 -cp "$W/tools/patch:$W/tools/asm/*" Patch $B $W/rlib/*.jar $W/libs/*.jar 2>&1 | grep -v JAVA_TOOL
cp $W/assets_new/shaders/core/* $B/assets/frieren_arcana/shaders/core/
mkdir -p $B/assets/frieren_arcana/sounds/breaker
cp $W/assets_new/sounds/breaker/*.ogg $B/assets/frieren_arcana/sounds/breaker/
cp $W/assets_new/sounds.json $B/assets/frieren_arcana/sounds.json
python3 - "$B" <<'PY'
import json, sys
p = sys.argv[1] + '/assets/frieren_arcana/lang/en_us.json'
d = json.load(open(p))
d['subtitles.frieren_arcana.breaker'] = 'Barrier Breaker'
json.dump(d, open(p, 'w'), indent=2, ensure_ascii=False)
PY
sed -i "s/^version=\"1\.5\.10\"/version=\"$V\"/" $B/META-INF/neoforge.mods.toml
grep -q "version=\"$V\"" $B/META-INF/neoforge.mods.toml
python3 $W/gen/pack.py /mnt/user-data/outputs/frieren-arcana-neoforge-1_21_1-1_5_10.jar $B $W/frieren-arcana-neoforge-1_21_1-$VU.jar
