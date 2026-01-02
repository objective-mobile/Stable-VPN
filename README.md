# StableVPN

A modern, secure VPN application for Android built with Kotlin and Jetpack Compose, utilizing
OpenVPN protocol for reliable and encrypted connections.

## 📱 App Description

StableVPN is a comprehensive VPN solution designed to provide users with secure, private, and stable
internet connections. The app features a clean, intuitive interface built with Material Design 3
principles and offers robust VPN functionality powered by the OpenVPN protocol.

### Key Features

- 🔒 **Secure VPN Connection** - OpenVPN protocol implementation for maximum security
- 🌍 **Multiple Server Locations** - Connect to servers worldwide
- 📱 **Modern UI** - Built with Jetpack Compose and Material Design 3
- 🔔 **Smart Notifications** - Real-time connection status updates
- 🛡️ **Permission Management** - Intelligent permission handling system
- 📊 **Connection Analytics** - Firebase integration for app insights
- 💰 **Monetization Ready** - Integrated AdMob advertising system

## 🏗️ Architecture

StableVPN follows **Clean Architecture** principles with a modular structure designed for
maintainability, testability, and separation of concerns.

### Module Structure

```
StableVPN/
├── app/                    # Main application module
│   ├── data/              # App-level data implementations
│   ├── domain/            # App-level business logic
│   └── presentation/      # UI components and screens
├── countries/             # Country management module
│   ├── data/              # Country data sources
│   └── domain/            # Country business logic
├── vpn/                   # VPN functionality module
│   ├── data/              # VPN data implementations
│   └── domain/            # VPN business logic
├── permissions/           # Permission handling module
│   ├── data/              # Permission data layer
│   └── domain/            # Permission business logic
└── advertising/           # Advertising module
    ├── data/              # Ad data management
    ├── domain/            # Ad business logic
    └── presentation/      # Ad UI components
```

### Architecture Principles

- **Domain Layer**: Pure business logic with no Android dependencies
- **Data Layer**: Repository implementations and external service integration
- **Presentation Layer**: UI components, ViewModels, and user interactions
- **Dependency Inversion**: Higher-level modules depend on abstractions
- **Single Responsibility**: Each module has a specific, well-defined purpose

## 🛠️ Technology Stack

### Core Technologies

- **Language**: Kotlin 2.2.20
- **UI Framework**: Jetpack Compose (BOM 2025.09.01)
- **Architecture**: Clean Architecture + MVVM
- **Dependency Injection**: Manual DI (no frameworks)
- **Async Programming**: Kotlin Coroutines 1.8.0

### Key Libraries

#### Android Jetpack

- **Compose**: Modern declarative UI toolkit
- **ViewModel**: Lifecycle-aware UI state management
- **Activity**: Modern activity handling
- **Core KTX**: Kotlin extensions for Android APIs

#### Networking & Security

- **OpenVPN**: Secure VPN protocol implementation
- **OkHttp 4.10.0**: HTTP client for network operations
- **BouncyCastle 1.69**: Cryptographic library for security operations

#### Firebase Integration

- **Firebase BOM 34.7.0**: Centralized version management
- **Firebase Analytics**: User behavior and app performance insights
- **Firebase Firestore**: Cloud database for app data
- **Firebase Crashlytics**: Real-time crash reporting

#### Monetization

- **Google AdMob 24.9.0**: Banner and interstitial advertising
- **Play Services Ads**: Advanced advertising features

#### Development Tools

- **Android Gradle Plugin 8.13.0**: Build system
- **Material Design 3**: Modern design system
- **JUnit**: Unit testing framework

## 🔐 OpenVPN Protocol

StableVPN leverages the OpenVPN protocol for secure VPN connections:

### Protocol Features

- **Strong Encryption**: AES-256 encryption for data protection
- **Authentication**: RSA certificates and pre-shared keys
- **Flexibility**: Support for UDP and TCP protocols
- **Reliability**: Automatic reconnection and network change handling
- **Cross-Platform**: Industry-standard protocol compatibility

### Security Implementation

- Certificate-based authentication
- Perfect Forward Secrecy (PFS)
- DNS leak protection
- Kill switch functionality
- Secure key exchange protocols

## 🚀 Getting Started

### Prerequisites

- Android Studio Arctic Fox or later
- Android SDK 23+ (minimum) / 36 (target)
- Kotlin 2.2.20+
- Java 8+

### Setup Instructions

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/stablevpn.git
   cd stablevpn
   ```

2. **Configure local properties**
   Create `local.properties` file in the root directory:
   ```properties
   sdk.dir=/path/to/your/android/sdk
   
   # AdMob Configuration
   BANNER_ID=your_banner_ad_unit_id
   INTERSTITIAL_ID=your_interstitial_ad_unit_id
   ADMOB_APPLICATION_ID=your_admob_app_id
   
   # Release Signing (optional)
   RELEASE_STORE_FILE=path/to/keystore
   RELEASE_STORE_PASSWORD=your_store_password
   RELEASE_KEY_ALIAS=your_key_alias
   RELEASE_KEY_PASSWORD=your_key_password
   ```

3. **Add Firebase configuration**
    - Create a Firebase project
    - Download `google-services.json`
    - Place it in the `app/` directory

4. **Build and run**
   ```bash
   ./gradlew assembleDebug
   ```

### Configuration

#### AdMob Setup

The app uses build config fields for AdMob integration:

- `BuildConfig.BANNER_ID`: Banner ad unit ID
- `BuildConfig.INTERSTITIAL_ID`: Interstitial ad unit ID
- Manifest placeholder for Application ID

#### Firebase Setup

- Analytics for user behavior tracking
- Crashlytics for crash reporting
- Firestore for cloud data storage

## 📁 Project Structure

### Naming Conventions

Following **Elegant Objects** principles:

- **Domain Objects**: Nouns only (`VpnConnection`, `Country`, `Permission`)
- **Repository Interfaces**: `NounRepository` (`VpnRepository`, `CountryRepository`)
- **Data Implementations**: `NounBaseRepository` (`VpnBaseRepository`)
- **ViewModels**: `ScreenNameViewModel` (`VpnViewModel`, `PermissionsViewModel`)
- **Composables**: Descriptive names (`VpnScreen`, `PermissionsScreen`)

### Package Organization

```
com.objmobile.{module}/
├── data/
│   ├── repository/        # Repository implementations
│   ├── datasource/        # Data sources (API, local)
│   └── model/             # Data models
├── domain/
│   ├── model/             # Domain entities
│   ├── repository/        # Repository interfaces
│   └── object/            # Business objects
└── presentation/          # UI layer (app module only)
    ├── components/        # Reusable UI components
    ├── screen/            # Screen composables
    ├── viewmodel/         # ViewModels
    └── theme/             # UI theming
```

## 🧪 Testing

### Testing Strategy

- **Unit Tests**: Domain layer business logic
- **Integration Tests**: Repository implementations
- **UI Tests**: Compose screen components
- **Preview Functions**: Visual component validation

### Running Tests

```bash
# Unit tests
./gradlew test

# Instrumented tests
./gradlew connectedAndroidTest

# All tests
./gradlew check
```

## 🔧 Build Configuration

### Build Types

- **Debug**: Development builds with debugging enabled
- **Release**: Production builds with ProGuard optimization

### Signing Configuration

Release builds use signing configuration from `local.properties`:

- Store file, password, key alias, and key password
- Automatic signing for release builds

## 📱 Permissions

The app requires the following permissions:

- `INTERNET`: Network access for VPN connections
- `ACCESS_NETWORK_STATE`: Monitor network connectivity
- `FOREGROUND_SERVICE`: VPN service operation
- `CHANGE_NETWORK_STATE`: VPN network configuration
- `ACCESS_WIFI_STATE`: WiFi network monitoring
- `CHANGE_WIFI_STATE`: WiFi configuration changes
- `POST_NOTIFICATIONS`: Connection status notifications

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Follow the established architecture patterns
4. Add tests for new functionality
5. Commit your changes (`git commit -m 'Add amazing feature'`)
6. Push to the branch (`git push origin feature/amazing-feature`)
7. Open a Pull Request

### Code Style

- Follow Kotlin coding conventions
- Use meaningful variable and function names
- Implement proper error handling
- Add documentation for public APIs
- Follow Clean Architecture principles

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙏 Acknowledgments

- OpenVPN community for the robust VPN protocol
- Android Jetpack team for modern development tools
- Material Design team for excellent design guidelines
- Firebase team for comprehensive backend services

## 📞 Support

For support, email support@stablevpn.com or create an issue in this repository.

---

**StableVPN** - Secure, Stable, Simple VPN Solution