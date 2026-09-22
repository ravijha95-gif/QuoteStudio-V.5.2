# QuoteStudio — One-Click APK Build (V5)

This version is configured for GitHub Actions and does not require Android Studio.

## Build
1. Upload the contents of this project to your GitHub repository.
2. Open **Actions**.
3. Select **Build QuoteStudio APK**.
4. Click **Run workflow**.
5. Download the **QuoteStudio-debug-apk** artifact.
6. Inside the artifact is `app-debug.apk`.

## Important
The workflow creates `.env.example` automatically if a browser upload omitted the dotfile. It uses a placeholder key only so Gradle can configure the project. A real Gemini/Firebase configuration may be required for the app's online AI features.

The debug APK is intended for testing/sideloading. A Play Store release should use a real release keystore and signed AAB.
