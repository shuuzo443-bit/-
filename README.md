# ボウリング管理 Android APK

ボールの追加・編集・削除、メーカー/素材フィルター、RG/ΔRGソート、詳細表示、公式サイト検索、端末内保存を備えたAndroidアプリです。初期データは空です。

## スマホだけでAPKを作る（GitHub）
1. GitHubで新しい公開または非公開のリポジトリを作成します。
2. このZIPを展開し、**BowlingManagerフォルダの中身**をリポジトリのルートへアップロードします（`.github/workflows/build-apk.yml`も必要）。
3. GitHubの **Actions → Build Android APK → Run workflow** を押します。アップロード時に自動実行される場合もあります。
4. 緑のチェックが付いた実行結果を開き、下部の **Artifacts → BowlingManager-debug-apk** をダウンロードします。
5. ZIPを展開し `app-debug.apk` をAndroidで開いてインストールします。

※ GitHub Actionsの実行権限が必要です。APKはデバッグ署名版です。Google Play公開用ではありません。
※ 初回ビルドはネット接続が必要です。実際のビルド成功はこの環境では未検証です。
※ データは端末のアプリ保存領域に格納されます。アンインストール時に失われる場合があります。
※ 公式サイトの検索はブラウザ検索です。メーカーサイトからのスペック自動抽出は未実装です。
