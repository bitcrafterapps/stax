## Relevant Files

- `app/build.gradle.kts` - To add dependencies for new features (e.g., Firebase, charting libraries, TFLite).
- `app/src/main/java/com/example/stax/data/models.kt` - To update the `Session` data class with `buyInAmount` and `cashOutAmount`.
- `app/src/main/java/com/example/stax/data/viewmodels.kt` - To handle the logic for session analytics, community feeds, and user profiles.
- `app/src/main/java/com/example/stax/navigation/AppNavigation.kt` - To add routes for new screens like the Community Feed and User Profiles.
- `app/src/main/java/com/example/stax/ui/screens/SessionsScreen.kt` - Will be heavily modified to include analytics, P/L tracking, and a list view.
- `app/src/main/java/com/example/stax/ui/screens/PhotoTakingScreen.kt` - (New File) To be created to handle camera functionality with overlays and filters.
- `app/src/main/java/com/example/stax/ui/screens/CommunityScreen.kt` - (New File) To be created for the community feed.
- `app/src/main/java/com/example/stax/ui/screens/ProfileScreen.kt` - (New File) To be created for user profiles.

### Notes

- For Android, run tests via Android Studio or `./gradlew test` for unit tests and `./gradlew connectedAndroidTest` for instrumented tests.

## Tasks

- [ ] 1.0 Implement Photo Experience Enhancements
  - [ ] 1.1 Add camera overlay guides (e.g., a circle or rectangle) to help with photo composition.
  - [ ] 1.2 Implement basic photo editing tools: crop, rotate, brightness, and contrast.
  - [ ] 1.3 Create and apply pre-made photo filters tailored for poker environments (e.g., "Felt Green," "Vegas Neon").
  - [ ] 1.4 (Stretch Goal) Research and integrate a TensorFlow Lite model for AI chip value recognition.
- [ ] 2.0 Build Community & Sharing Features
  - [ ] 2.1 Integrate Android's native Share Sheet for posting photos to social media.
  - [ ] 2.2 Pre-populate shared content with session details like casino name and a hashtag.
  - [ ] 2.3 Design and implement a new "Community" tab/screen in the main navigation.
  - [ ] 2.4 Create functionality for users to publicly share photos to the community feed.
  - [ ] 2.5 Implement "like" and "comment" features on community posts.
  - [ ] 2.6 Create simple user profiles to display a user's shared photos and basic stats.
- [ ] 3.0 Implement Session Tracking and Analytics
  - [ ] 3.1 Modify the `Session` data model in `models.kt` to include `buyInAmount` and `cashOutAmount` fields.
  - [ ] 3.2 Update the UI to allow users to input buy-in and cash-out amounts when creating/editing a session.
  - [ ] 3.3 Automatically calculate and display the Profit/Loss for each session.
  - [ ] 3.4 Redesign the `SessionsScreen` from a photo grid to a data-driven list view.
  - [ ] 3.5 Display key session data (Casino, Date, Game, P/L) in list items, with color-coding for profit/loss.
  - [ ] 3.6 Add a summary section at the top of the `SessionsScreen` for key stats (Total P/L, ROI, Win Rate).
  - [ ] 3.7 Implement a line chart on the `SessionsScreen` to visualize bankroll progression over time.
- [ ] 4.0 Add Advanced Session Functionality
  - [ ] 4.1 Add a search bar to the `SessionsScreen` to filter sessions by casino name or game type.
  - [ ] 4.2 Implement filter options to show sessions by date range.
  - [ ] 4.3 Implement filter options to show only winning or losing sessions.
- [ ] 5.0 Implement General App Improvements
  - [ ] 5.1 (Pro Feature) Integrate Firebase Firestore for cloud backup and synchronization of all user data.
  - [ ] 5.2 Implement a "Data Export" feature to save all session data to a CSV file.
  - [ ] 5.3 Design and implement an achievement system with badges for reaching milestones (e.g., "First Session," "Vegas Vet"). 