# Generic IoT Infrastructure

## Project Overview

**Generic IoT Infrastructure** is a Java-based project designed to provide a service for companies to manage their Internet of Things (IoT) devices. The infrastructure consists of a web interface for companies to register themselves and their product types, a backend server to handle device registration and updates, and various modules implementing well-known design patterns. This project supports HTTP, TCP, and UDP communication for device interactions with the server.

## Project Structure

The project is split into two main components:
- **Website**: Located in the `website` directory, this part contains the user-facing web interface for companies to register themselves and their product types. The website is powered by HTML, CSS, and Java Servlets and runs on Tomcat.
- **GatewayServer**: Located in the `GatewayServer` directory, this is the backend server where the core functionality takes place, such as device registration, handling updates, and dynamic loading of command implementations. The server supports HTTP, TCP, and UDP requests.

## Features

- **Company Registration**: Companies can register themselves on the platform via the web interface. 
- **Product Registration**: After registering, companies can register product types (but not individual product devices) through the web interface.
- **Device Registration**: Devices can register with the server (independent of the web interface) by sending requests directly to the server.
- **Device Updates**: After registration, devices can start sending updates to the server via HTTP, TCP, or UDP requests.

## Modules and Design Patterns

The server (`GatewayServer`) includes several self-implemented modules, some of which make use of classic design patterns:

### 1. **Command Design Pattern**  
This pattern is used to encapsulate all device actions (commands) as objects, allowing flexible command processing.

### 2. **Factory Design Pattern**  
The server utilizes the Factory pattern for creating and managing the command objects dynamically.

### 3. **ThreadPool Design Pattern**  
A custom thread pool is used to manage concurrent operations, ensuring optimal resource usage and performance.

### 4. **BlockingPriorityQueue**  
A blocking priority queue is used for managing tasks with varying priorities.

### 5. **DirectoryMonitor**  
This module monitors specific directories for changes (e.g., new files or updates) and triggers the necessary actions.

### 6. **DynamicJarLoader**  
This module dynamically loads jar files that contain implementations of the `Command` interface, making the server extensible without requiring recompilation.

### 7. **Plug and Play (P & P) Module**  
The P & P module utilizes the `DirectoryMonitor` and `DynamicJarLoader` to monitor the `JARfiles` directory in the `GatewayServer`. Users can compile and place jar files containing new `Command` implementations into this directory, and the server will automatically load them dynamically into the Factory.

## How It Works

1. **Company Registration**:  
   - Companies access the web interface (located in the `website` directory) and register themselves.
   - Once registered, they can start adding product types.

2. **Product Registration**:  
   - After registering, companies can register their product types via the web interface.
   
3. **Device Registration**:  
   - Devices send HTTP, TCP, or UDP requests to the server to register their device type. 
   - Once registered, they can send updates to the server.

4. **Dynamic Command Loading**:  
   - Users compile command classes into jar files and place them in the `JARfiles` directory in the `GatewayServer`.
   - The `DirectoryMonitor` continuously watches this directory and, when a new jar is added, the `DynamicJarLoader` loads it into the server, allowing it to be used by the `Factory`.

## Technologies Used

- **Java**: The primary language for both the website and server-side logic.
- **Servlets**: Used for server-side logic in the web interface.
- **Tomcat**: Web server for hosting the website.
- **HTML, CSS**: Used for the front-end of the website.
- **TCP, UDP, HTTP**: Protocols supported by the server for device communication.

## Getting Started

### Prerequisites

- JDK 8 or higher
- Apache Tomcat (for the web interface)
- Maven or Gradle for dependency management (optional but recommended for building)

### Installation

1. Clone this repository:
   ```bash
   git clone https://github.com/yourusername/GenericIoTInfrastructure.git
   ```
   
2. Setup the Web Interface:
   Deploy the website directory to your Tomcat server.
   
3. Setup the Gateway Server:
    Navigate to the GatewayServer directory and ensure the server is configured properly.
   
4. Using the P&P Module:
   For basic functionality, compile the Command implementations into jar files and place them in the JARfiles directory inside the GatewayServer directory.
   Any additional functionality can be added due to Command implementation and compilation into a .jar file, placing it in the JARfiles directory.
   The DirectoryMonitor will detect new jar files and load them into the system automatically.

