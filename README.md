# MovieExplore_Li

## Overview
MovieExplore_Li is an Android application that allows users to explore movies, view their details, and manage their favorites. This project demonstrates modern Android development practices by integrating key technologies such as:

- **Web API**: Fetching movie data from an online movie database.
- **MVVM Architecture**: Adopting a clean architecture for better separation of concerns.
- **LiveData**: Observing and reacting to data changes in real time.
- **ViewBinding**: Efficiently binding UI components in a type-safe manner.
- **Firebase**: Enabling authentication and managing user-specific data in the cloud.

## Features

1. **Movie Search**
   - Users can search for movies by title.
   - Displays a list of movies matching the search query.

2. **Movie Details**
   - View detailed information about a selected movie, including:
     - Poster
     - Title
     - Year of release
     - Plot
   - Add or remove movies from favorites.

3. **Favorites Management**
   - Save movies to a personal favorites list.
   - View and manage the list of favorite movies.

4. **Firebase Authentication**
   - User login and registration.
   - Securely manage user-specific data in the cloud.

5. **MVVM Architecture**
   - Ensures separation of UI and business logic.
   - Uses `ViewModel` and `LiveData` for efficient data flow.

6. **Navigation**
   - Intuitive navigation between login, search, details, and favorites screens.
   - Back button behavior:
     - Physical back button in `FavoriteDetailsActivity` returns to the "My Favorites" page.
     - Toolbar back button navigates to "My Favorites" as well.

## Technologies Used

- **Programming Language**: Java
- **Architecture**: MVVM (Model-View-ViewModel)
- **UI Components**:
  - RecyclerView for displaying movie lists.
  - ViewBinding for type-safe UI binding.
- **API Integration**:
  - Retrofit for network requests.
  - OMDB API for fetching movie data.
- **Firebase**:
  - Firebase Authentication for user login and registration.
  - Firestore for storing and managing favorite movies.
- **LiveData**:
  - Observes changes in movie data in real-time.

## Screens

1. **Login Screen**
   - Allows users to log in or navigate to the registration screen.

2. **Register Screen**
   - Enables new users to create an account.

3. **Movie Search Screen**
   - Search for movies using a query.
   - Displays a list of results with movie posters and titles.

4. **Movie Details Screen**
   - Detailed information about a selected movie.
   - Add or remove the movie from favorites.

5. **Favorites Screen**
   - Displays a list of movies added to the user's favorites.
   - Navigate to detailed information for each favorite movie.

## Project Structure

- **Model**: Contains data classes (`MovieModel`, `MovieResponse`).
- **ViewModel**: Implements the business logic and interacts with the repository.
- **View**: Activities and UI components (`Login`, `Register`, `MovieSearchActivity`, `MovieDetailsActivity`, `MyFavoriteActivity`, `FavoriteDetailsActivity`).
- **Network**: Handles API requests using Retrofit (`ApiClient`, `ApiService`).
- **Firebase**: Manages authentication and Firestore database.

## Getting Started

### Prerequisites

- Android Studio (latest version recommended)
- A Firebase project configured with:
  - Authentication enabled.
  - Firestore database set up.
- OMDB API key for fetching movie data.

### Steps to Run the Project

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/JoeJoeLXD/MovieExplore_Li.git
   ```

2. **Open in Android Studio**:
   - Open the project folder in Android Studio.

3. **Configure Firebase**:
   - Add the `google-services.json` file to the `app` directory.
   - Enable Email/Password authentication in Firebase.

4. **Add OMDB API Key**:
   - Add your OMDB API key to the `strings.xml` file:
     ```xml
     <string name="omdb_api_key">5d005d1</string>
     ```

5. **Build and Run**:
   - Sync Gradle files.
   - Build and run the app on an emulator or physical device.

## Future Improvements

- Add more filters and sorting options for movies.
- Enhance error handling and offline support.
- Implement Jetpack Compose for a modern UI approach.

## Contributing

Contributions are welcome! Please fork the repository and submit a pull request with your changes.

## License

This project is licensed under the MIT License. See the LICENSE file for details.

## Contact

For any questions or feedback, please contact:
- **Author**: Xiangdong Li
- **Email**: xd18@me.com

Enjoy exploring movies with MovieExplore_Li!

