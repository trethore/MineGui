file_name="minegui-src.txt"

rm ./"${file_name}"
recap -o "${file_name}" -I "^(libs|src|gradle.properties|settings.gradle|build.gradle|README)"
