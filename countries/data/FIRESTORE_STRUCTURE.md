# Firestore Structure for Countries Module

## Collection: `countries`

The countries data is stored in a Firestore collection named `countries`. Each document represents a
single country.

### Document Structure

```json
{
  "countryName": "United States",
  "countryConfig": "us-config-data"
}
```

### Fields

- **countryName** (String): The display name of the country
- **countryConfig** (String): Configuration data specific to the country

### Example Documents

```json
// Document ID: auto-generated or custom
{
  "countryName": "United States",
  "countryConfig": "us-vpn-servers-config"
}

// Document ID: auto-generated or custom  
{
  "countryName": "Germany",
  "countryConfig": "de-vpn-servers-config"
}

// Document ID: auto-generated or custom
{
  "countryName": "Japan", 
  "countryConfig": "jp-vpn-servers-config"
}
```

## Usage in Code

The `FirestoreCountryDataSource` class handles all Firestore operations:

- `fetchCountries()`: Retrieves all countries from the collection
- `fetchCountryByName(countryName)`: Retrieves a specific country by name

## Security Rules

Make sure your Firestore security rules allow reading from the countries collection:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Allow read access to countries collection
    match /countries/{document} {
      allow read: if true; // Adjust based on your security requirements
    }
  }
}
```

## Setup

1. Ensure Firebase is properly configured in your Android project
2. Add countries data to the `countries` collection in Firestore
3. Configure appropriate security rules
4. The repository will automatically fetch data using the provided structure