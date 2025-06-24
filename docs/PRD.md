# Chip Porn - Product Requirements Document (PRD)

## 1. Overview

Chip Porn is a mobile application designed for poker enthusiasts to capture, organize, and browse photos of their poker chip stacks. The app provides a "photo gallery" experience, allowing users to create sessions for different poker games and locations, add photos to those sessions, and view them in a gallery format.

## 2. Core Features

### 2.1. Session Management

The primary organizational unit in Chip Porn is the "Session." A session represents a specific poker game or event.

-   **Create New Session**:
    -   Users can create a new session from the main "Photos" screen by tapping the floating action button.
    -   An "Add New Session" dialog appears, prompting the user for the following information:
        -   **State**: A dropdown menu to select the US state where the session took place.
        -   **Casino Name**: A dependent dropdown menu that populates with casinos in the selected state. This data is sourced from a local `casinos.json` file.
        -   **Game Type**: A text field for the user to specify the type of game (e.g., "$1/$2 NLH," "Omaha 8," "2/5 PLO").
        -   **Session Type**: A selector to specify whether the session was for "Cash" or a "Tourney."
    -   The session is created upon confirming the details.

-   **View Sessions**:
    -   Sessions are displayed as folders on the main "Photos" screen in a two-column grid.
    -   Each session folder displays the following information:
        -   The name of the casino.
        -   The game type.
        -   The number of photos in the session.
        -   An icon indicating whether it was a "Cash" or "Tourney" session.
        -   The most recently added photo as the folder's background thumbnail. If no photos exist, a generic folder icon is shown.

-   **Delete Session**:
    -   Users can delete a session by long-pressing on a session folder.
    -   A long-press reveals a delete icon.
    -   Tapping the delete icon brings up a confirmation dialog to prevent accidental deletion.
    -   Confirming the deletion removes the session and all associated photos from both the app's database and the device's file system.

### 2.2. Photo Gallery

Each session has its own photo gallery.

-   **View Photo Gallery**:
    -   Tapping on a session folder navigates the user to the photo gallery screen for that session.
    -   The gallery displays all photos for the selected session in a grid of large, square tiles.
    -   Each photo tile has an overlay with a subtle gradient scrim at the bottom, displaying the photo's rating.

-   **Add Photos**:
    -   Users can add new photos to a session from the photo gallery screen.
    -   The app provides an option to take a new photo using the device's camera.

-   **Delete Photos**:
    -   Users can delete individual photos from within the gallery.

### 2.3. Full-Screen Image Viewer

-   **View Full-Screen Image**:
    -   Tapping on a photo in the gallery opens it in a full-screen viewer.
    -   The viewer displays the image in high resolution.

-   **Swipe Navigation**:
    -   Users can swipe left and right to navigate between photos in the same session without returning to the gallery view.

-   **Rate Photos**:
    -   The full-screen viewer includes a rating bar (1-5 stars) that allows the user to rate each photo.
    -   The rating is saved to the database and is reflected on the photo's tile in the gallery.

## 3. User Interface and Navigation

### 3.1. Main Navigation

-   **Bottom Navigation Bar**: The app uses a bottom navigation bar for top-level navigation between three main sections:
    -   **Photos**: The main screen, displaying the session folders.
    -   **Sessions**: A placeholder screen.
    -   **About**: A screen displaying app information.

### 3.2. Screens

-   **Splash Screen**:
    -   A brief splash screen is displayed when the app starts, showing the app's logo.

-   **Photos Screen (Dashboard)**:
    -   The main screen of the app.
    -   Displays the "Chip Porn" title with the app logo.
    -   Shows a grid of all created sessions.

-   **Photo Gallery Screen**:
    -   Displays photos for a specific session.
    -   The top app bar shows the name of the current session.

-   **About Screen**:
    -   Displays the app's logo and the "Chip Porn" title.
    -   Includes the tagline "Poker Porn, No Shame."
    -   Shows the app version number (v.1.00.00).

### 3.3. Visual Design

-   **Theme**: The app features a dark theme with a custom purple-to-black gradient background applied to all screens.
-   **Logo**: A custom "infinity glasses" logo is used on the splash screen, photos screen, and about screen.
-   **Typography**: Clean, modern typography is used throughout the app to ensure readability against the dark, gradient background.

## 4. Technical Details

-   **Database**: The app uses a local Room database to store session information and photo metadata (paths, ratings, etc.).
-   **File Storage**: Photos taken within the app are saved to the device's internal storage.
-   **Data**: Casino data is populated from a `casinos.json` file included in the app's assets. 