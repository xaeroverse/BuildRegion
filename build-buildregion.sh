#!/bin/bash
set -e

JAR=$PWD/BuildRegion-1.0.2.zip

rm $JAR || true

# ugh, these scripts exit with success status even if they failed...
# as a workaround check their output directories

echo "== Compiling =="
./recompile.sh
[ "$(ls bin/minecraft/net/minecraft/src/)" ] || exit 1

echo "== Reobfuscating =="
./reobfuscate.sh
[ "$(ls reobf/minecraft/)" ] || exit 1

echo "== Packaging $JAR =="
cd reobf/minecraft/
rm -rf META-INF
#jar cfv $JAR ./
zip -r $JAR *
