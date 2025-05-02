# Environment Manager Plugin for IntelliJ IDEA

Environment Manager is an IntelliJ IDEA plugin that helps you manage environment variables for your run configurations. It allows you to create multiple sets of environment variables (e.g., development, staging, production) and easily switch between them.

## Features

- Create, edit, and delete environment variable sets
- Add, edit, and remove environment variables within each set
- Activate a specific set to inject its environment variables into run configurations
- Persistent storage of environment variable sets between IDE restarts
- Simple and intuitive UI integrated into the IDE

## Installation

### From JetBrains Marketplace

1. Open IntelliJ IDEA
2. Go to Settings/Preferences > Plugins
3. Click on "Marketplace" tab
4. Search for "Environment Manager"
5. Click "Install"
6. Restart IntelliJ IDEA when prompted

### Manual Installation

1. Download the latest release `.zip` file from the [Releases](https://github.com/yelynnkhant/env-manager/releases) page
2. Open IntelliJ IDEA
3. Go to Settings/Preferences > Plugins
4. Click on the gear icon and select "Install Plugin from Disk..."
5. Navigate to the downloaded `.zip` file and select it
6. Restart IntelliJ IDEA when prompted

## Usage

1. Open the Environment Manager tool window by clicking on the "Environment Manager" tab on the right side of the IDE
2. Create a new environment variable set by clicking the "Add Set" button
3. Add environment variables to the set by selecting it and clicking the "Add Variable" button
4. Enter the key and value for each environment variable
5. Activate a set by selecting it and clicking the "Activate Set" button
6. Run your application - the environment variables from the active set will be injected into the run configuration

## Project Structure

The plugin is organized into the following packages:

- `cloud.yelynn.envmanager.model`: Contains the data models for environment variables and sets
- `cloud.yelynn.envmanager.service`: Contains the service for managing and persisting environment variable sets
- `cloud.yelynn.envmanager.ui`: Contains the UI components for the plugin
- `cloud.yelynn.envmanager.run`: Contains the components for integrating with IntelliJ's run configuration system

### Key Components

- `EnvironmentVariable`: Represents a single environment variable with a key and value
- `EnvironmentVariableSet`: Represents a set of environment variables (e.g., dev, staging, prod)
- `EnvironmentVariableService`: Service for managing and persisting environment variable sets
- `EnvironmentManagerToolWindowFactory`: Factory for creating the Environment Manager tool window
- `EnvironmentManagerToolWindowContent`: Content for the Environment Manager tool window
- `EnvironmentManagerService`: Service that injects environment variables into run configurations

## Development Setup

### Prerequisites

- IntelliJ IDEA (Community or Ultimate edition)
- Java Development Kit (JDK) 21 or later
- Gradle 8.4 or later

### Setting Up the Development Environment

1. Clone the repository:
   ```
   git clone https://github.com/yelynnkhant/env-manager.git
   cd env-manager
   ```

2. Open the project in IntelliJ IDEA:
   - Select "Open" from the welcome screen
   - Navigate to the cloned repository and select it
   - Choose "Open as Project"

3. Build the project:
   - From the terminal: `./gradlew build`
   - Or use the Gradle tool window in IntelliJ IDEA

4. Run the plugin in a development instance of IntelliJ IDEA:
   - From the terminal: `./gradlew runIde`
   - Or use the "Run Plugin" run configuration in IntelliJ IDEA

## Building from Source

To build the plugin from source:

1. Clone the repository:
   ```
   git clone https://github.com/yelynnkhant/env-manager.git
   cd env-manager
   ```

2. Build the plugin:
   ```
   ./gradlew buildPlugin
   ```

3. The built plugin will be available in the `build/distributions` directory

## Contributing

Contributions are welcome! If you'd like to contribute to the project, please follow these steps:

1. Fork the repository
2. Create a new branch for your feature or bugfix
3. Make your changes
4. Submit a pull request

Please ensure your code follows the existing style and includes appropriate tests.

## License

This project is licensed under the MIT License - see the LICENSE file for details.