# Goose Android

A feature-rich Android client for Goose AI Agent, built with Jetpack Compose.

## What's New in v1.1.0

### Major Features Added
- **Local Database Persistence** - Messages and sessions now persist locally with Room
- **Offline Support** - Queue messages when offline and retry automatically
- **Message Actions** - Copy, bookmark, delete messages with context menus
- **Advanced Settings** - Theme selection, text size, security options
- **Pull-to-Refresh** - Refresh conversations by pulling down
- **Search** - Search through sessions and messages
- **Session Management** - Pin, archive, rename, delete sessions
- **Biometric Security** - Optional fingerprint/face unlock
- **Certificate Pinning** - Enhanced security for API connections
- **Export Data** - Export chat history to JSON

## Core Features

- **Chat Interface**: Send messages and receive streaming responses from the Goose API
- **Session Management**: View, create, resume, pin, archive, rename chat sessions
- **Settings**: Configure server URL, secret key, appearance, and security
- **Trial Mode**: Connect to the demo server by default
- **Themes**: System, Light, and Dark modes
- **Text Sizes**: Small, Normal, Large, Extra Large
- **Message Status**: See pending, sending, sent, failed states
- **Database Persistence**: Local storage with Room for offline access
- **Full-Text Search**: Search through all messages

## Architecture

The app follows clean architecture principles:

- **UI Layer**: Jetpack Compose with Material 3
- **ViewModel**: MVVM with StateFlow for reactive state
- **Data Layer**: Repository pattern with Room + Retrofit/OkHttp
- **Dependency Injection**: Hilt for DI
- **Background Work**: WorkManager for offline message sync
- **Security**: Encrypted SharedPreferences for credentials

## Project Structure

```
app/src/main/java/com/block/goose/
├── GooseApplication.kt           # Application with Hilt
├── MainActivity.kt               # Navigation setup
├── di/
│   └── DatabaseModule.kt         # Hilt module for Room
├── data/
│   ├── db/
│   │   ├── AppDatabase.kt        # Room database
│   │   ├── dao/                  # Data Access Objects
│   │   │   ├── SessionDao.kt
│   │   │   ├── MessageDao.kt
│   │   │   ├── PendingMessageDao.kt
│   │   │   └── BookmarkDao.kt
│   │   ├── entity/               # Room entities
│   │   │   ├── SessionEntity.kt
│   │   │   ├── MessageEntity.kt
│   │   │   ├── PendingMessageEntity.kt
│   │   │   └── BookmarkEntity.kt
│   │   └── SearchFtsEntity.kt  # FTS for search
│   ├── api/
│   │   ├── GooseApiService.kt    # API client with SSE
│   │   ├── SettingsRepository.kt # DataStore preferences
│   │   ├── UserSettings.kt       # App settings
│   │   └── ApiResult.kt
│   ├── model/
│   │   ├── Message.kt            # Message models
│   │   ├── ChatSession.kt        # Session models
│   │   ├── SSEEvent.kt           # SSE event types
│   │   └── ToolCall.kt
│   ├── repository/
│   │   ├── SessionRepository.kt
│   │   ├── MessageRepository.kt
│   │   ├── PendingMessageRepository.kt
│   │   ├── BookmarkRepository.kt
│   │   └── UserSettingsRepository.kt
│   └── security/
│       └── SecurePreferences.kt  # Encrypted storage
├── worker/
│   └── PendingMessageWorker.kt   # Background sync
├── ui/
│   ├── theme/
│   │   ├── Theme.kt              # Material 3 theme
│   │   ├── Type.kt
│   │   └── Color.kt
│   ├── components/
│   │   ├── ChatInputView.kt
│   │   ├── MessageBubble.kt      # With context menu
│   │   ├── WelcomeCard.kt
│   │   ├── ShimmerLoading.kt
│   │   └── SearchBar.kt
│   └── screens/
│       ├── HomeScreen.kt         # With search
│       ├── ChatScreen.kt         # With pull-to-refresh
│       ├── SettingsScreen.kt     # Enhanced settings
│       ├── HomeViewModel.kt
│       ├── ChatViewModel.kt
│       └── SettingsViewModel.kt
└── util/
    └── Utils.kt                  # Clipboard, Time, Retry
```

## Building

### Prerequisites
- Android Studio Arctic Fox (or newer)
- JDK 17+
- Android SDK 26+

### Build Steps

1. Clone the repository:
```bash
git clone https://github.com/yourusername/goose-android.git
cd goose-android
```

2. Open in Android Studio
3. Build the project:
```bash
./gradlew assembleDebug
```

## Configuration

### API Server
By default, the app connects to `https://demo-goosed.fly.dev` (Trial Mode).

To use your own Goose server:
1. Go to Settings
2. Enter your server URL
3. Enter your secret key
4. Tap "Test" to verify
5. Save

### Security Settings
- **Certificate Pinning**: Enable for production security
- **Biometric Authentication**: Require fingerprint/face unlock

## Database Schema

- **sessions**: Chat sessions with metadata
- **messages**: All messages with status tracking
- **pending_messages**: Queue for offline messages
- **messages_fts**: Full-text search index
- **bookmarks**: User bookmarked messages

## Offline Support

When offline:
1. Messages are queued locally
2. WorkManager periodically retries
3. Exponential backoff handling
4. Messages sync on reconnection

## Changelog

### v1.1.0 - Major Update
- Added Room database for local persistence
- Added offline message support
- Added message status tracking
- Added context menus
- Added theme/text size selection
- Added session management
- Added search
- Added encrypted storage
- Added certificate pinning
- Added biometric auth
- Added data export

### v1.0.0 - Initial Release
- Basic chat functionality
- Session management
- Settings
- Trial mode
