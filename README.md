# Asserta

Asserta is a desktop strategy game where deception, bluffing, and a healthy dose of luck are the keys to victory. Outsmart your opponents, master your poker face, and play your cards right to win.

---

## Prerequisites

To build and run this project, you will need:

* **Java Development Kit (JDK):** Version 21
* **Build Tool:** Apache Maven
* An IDE (IntelliJ IDEA, Eclipse, or VS Code) configured for Java 21 development.

---

## Getting Started

### 1. Clone the Repository
```bash
git clone https://github.com/leonidasAndrei/asserta.git
cd asserta
```

### 2. Run the Project

This project uses the official `javafx-maven-plugin`. You can boot up the game instantly from your terminal using:

```bash
mvn javafx:run
```

### 3. Packaging and Building

If you want to compile and package your application files into a target build:

```bash
mvn clean package
```

---

## Project Structure

The project follows the standard Maven resource layout, isolating layout definitions from program behavior:

```text
asserta/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── leonidasAndrei/
│   │   │           └── asserta/
│   │   │               ├── App.java          # Application entry point (configured in POM)
│   │   │               ├── Main.java         # Prototype for CLI / Alternative entry point
│   │   │               ├── controller/       # FXML View Controllers & UI Event Handlers
│   │   │               ├── model/            # Game state, actores, logic, & mechanics
│   │   │               └── utils/            # Helper classes & custom utility logic
│   │   │
│   │   └── resources/
│   │       └── com/
│   │           └── leonidasAndrei/
│   │               └── asserta/
│   │                   ├── fxml/             # FXML file layouts for game scenes & UI screens
│   │                   ├── css/              # Application stylesheets
│   │                   └── assets/           # Media, audio, and visual game assets
│   │                       ├── fonts/        
│   │                       └── images/       
│   │   
│   └── test/
│       └── java/
│           └── com/
│               └── leonidasAndrei/
│                   └── asserta/              # Unit tests handled by JUnit 4
│
└── pom.xml                                   # Project configuration & Maven dependencies
```

---
## Resources and Assets
 - Images and Sprites 
    - [Card Faces](https://unbent.itch.io/yewbi-playing-card-set-1)
    - [Potions](https://beast-pixels.itch.io/crafting-materials)
 - Fonts 
   - [Arcade](https://www.dafont.com/arcade-ya.font)
---

## Author & Maintainer

<p align="center">
  <a href="https://github.com/leonidasAndrei">
    <img src="https://github.com/leonidasAndrei.png" width="120px" style="border-radius: 50%; box-shadow: 0 4px 8px rgba(0,0,0,0.1);"/>
    <br />
    <b>leonidasAndrei</b>
  </a>
  <br />
  <sub>Lead Developer</sub>
</p>

---
