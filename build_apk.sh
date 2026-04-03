#!/usr/bin/env bash
set -euo pipefail

# ── paths ───────────────────────────────────────────────────────────────────
ANDROID_JAR=/usr/lib/android-sdk/platforms/android-23/android.jar
BUILD_TOOLS=/usr/lib/android-sdk/build-tools/debian
AAPT2=$BUILD_TOOLS/aapt2
DX=$BUILD_TOOLS/dx
ZIPALIGN=$BUILD_TOOLS/zipalign
APKSIGNER=$BUILD_TOOLS/apksigner

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
SRC_DIR=$PROJECT_DIR/app/src/main
RES_DIR=$SRC_DIR/res
MANIFEST=$SRC_DIR/AndroidManifest.xml
JAVA_SRC=$SRC_DIR/java

BUILD=$PROJECT_DIR/build_out
rm -rf "$BUILD"
mkdir -p "$BUILD/compiled_res" "$BUILD/gen" "$BUILD/classes" "$BUILD/apk_staging"

echo "==> 1. Compile resources with aapt2"
find "$RES_DIR" -type f | while read -r f; do
    $AAPT2 compile "$f" -o "$BUILD/compiled_res/" 2>/dev/null || true
done

echo "==> 2. Link resources — generate R.java + unsigned base APK"
$AAPT2 link \
    --manifest "$MANIFEST" \
    -I "$ANDROID_JAR" \
    -o "$BUILD/apk_staging/base.apk" \
    --java "$BUILD/gen" \
    --min-sdk-version 23 \
    --target-sdk-version 34 \
    --version-code 1 \
    --version-name "1.0" \
    "$BUILD/compiled_res/"*.flat

echo "==> 3. Compile Java sources"
find "$JAVA_SRC" -name "*.java" > "$BUILD/sources.txt"
find "$BUILD/gen" -name "*.java" >> "$BUILD/sources.txt"
javac \
    -source 1.8 -target 1.8 \
    -cp "$ANDROID_JAR" \
    -d "$BUILD/classes" \
    @"$BUILD/sources.txt"

echo "==> 4. Convert .class → classes.dex"
$DX --dex \
    --output="$BUILD/classes.dex" \
    "$BUILD/classes"

echo "==> 5. Add dex to APK"
cp "$BUILD/apk_staging/base.apk" "$BUILD/apk_staging/unsigned.apk"
(cd "$BUILD" && zip -j apk_staging/unsigned.apk classes.dex)

echo "==> 6. Zipalign"
$ZIPALIGN -f 4 "$BUILD/apk_staging/unsigned.apk" "$BUILD/apk_staging/aligned.apk"

echo "==> 7. Generate debug keystore (if needed)"
KEYSTORE=$HOME/.android/debug.keystore
if [ ! -f "$KEYSTORE" ]; then
    mkdir -p "$HOME/.android"
    keytool -genkeypair -v \
        -keystore "$KEYSTORE" \
        -alias androiddebugkey \
        -keyalg RSA -keysize 2048 -validity 10000 \
        -storepass android -keypass android \
        -dname "CN=Android Debug,O=Android,C=US"
fi

echo "==> 8. Sign APK"
$APKSIGNER sign \
    --ks "$KEYSTORE" \
    --ks-pass pass:android \
    --key-pass pass:android \
    --ks-key-alias androiddebugkey \
    --out "$PROJECT_DIR/RecipeBook-debug.apk" \
    "$BUILD/apk_staging/aligned.apk"

echo ""
echo "✓ Build complete: $PROJECT_DIR/RecipeBook-debug.apk"
$APKSIGNER verify --verbose "$PROJECT_DIR/RecipeBook-debug.apk" | head -5
