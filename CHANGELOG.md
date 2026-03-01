# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0] - 2024-XX-XX - Major Release

### Added
- **Database Persistence**: Full Room database with entities for sessions, messages, bookmarks, and pending messages
- **Offline Support**: Message queue with exponential backoff retry via WorkManager
- **Message Status Tracking**: Visual indicators for PENDING, SENDING, SENT, FAILED, DELIVERED states
- **Message Actions**: Long-press context menu with Copy, Bookmark, Delete, Retry options
- **Session Management**: Pin, archive, rename, and delete sessions
- **Search**: Full-text search through messages and sessions
- **Pull-to-Refresh**: Swipe to refresh conversations
- **Theme Selection**: System, Light, and Dark themes
- **Text Size Preferences**: Small, Normal, Large, Extra Large
- **Security**: Encrypted SharedPreferences for API credentials
- **Certificate Pinning**: Optional enhanced security for API connections
- **Biometric Authentication**: Optional fingerprint/face unlock
- **Data Export**: Export chat history to JSON
- **Auto-Cleanup**: Automatic deletion of old messages based on preference
- **Shimmer Loading**: Visual placeholders while loading
- **Error Recovery**: Automatic retry mechanisms for failed operations

### Changed
- Migrated to Hilt for dependency injection
- Updated to Material 3 Design System
- Improved error handling with structured error types
- Enhanced Settings screen with organized sections
- Upgraded dependencies to latest stable versions

### Security
- Added encrypted credential storage
- Implemented secure logging (secrets redacted)
- Added certificate pinning support
- Added biometric authentication option

## [1.0.0] - 2024-XX-XX - Initial Release

### Added
- Basic chat interface with streaming SSE responses
- Session management (create, resume, list)
- Simple settings configuration
- Trial mode connection to demo server
- Light/Dark theme (system-based)
- Settings persistence with DataStore

## Future Roadmap

### Planned for 1.2.0
- [ ] Voice input support
- [ ] Message reactions/emoji
- [ ] Attachment support (images, files)
- [ ] Push notifications
- [ ] WebDAV sync option
- [ ] Import conversations
- [ ] Share session link
- [ ] Quick replies/suggestions

### Under Consideration
- [ ] Widget support
- [ ] Wear OS companion
- [ ] Auto-sync across devices
- [ ] Custom themes
- [ ] Plugin system
- [ ] Custom shortcuts

---

## How to Update

### From 1.0.0 to 1.1.0
1. Backup your data (optional)
2. Install the new APK
3. Open the app - database will auto-migrate
4. Configure new settings as desired

### Database Migration
The app automatically handles database migrations:
- Schema version 1: Initial database with all tables
- Future versions will use Room's automatic migration

## Deprecations

None currently.

## Breaking Changes

### 1.1.0
- Minimum Android SDK remains 26 (API level 26)
- JDK 17+ required for build

## Acknowledgments

- Original Goose iOS app by Block
- Material Design 3 by Google
- Jetpack Compose team
- Room, Hilt, and WorkManager teams
