# Asserta

Asserta is a game of psychological warfare. Survival depends on your ability to read your opponent, execute flawless bluffs, and know when to trust your luck.

---

## How to Play


### 1. Deck Composition & Hand Rules
* **The Hand:** Every player starts the game with a hand of **5 cards**.
* **The Deck:** The playing deck consists of 20-card setup containing:
  * **6 Kings** (K)
  * **6 Queens** (Q)
  * **6 Aces** (A)
  * **2 Wildcards / Jokers** (Can legally act as the declared rank for the current round).

### 2. Turn Actions
On your turn, you have two choices:
* **Bluff / Play:** Place 1 to 3 cards face-down and declare that they match the current table round (e.g., King, Queen, or Ace). *You are not obligated to tell the truth.*
* **Call Bluff!:** Accuse the previous player of lying about their cards.

### 3. The Consequence
When a bluff is called, the played cards are shown:
* **If they lied:** The liar must pick one of the **4 drinks** on the table.
* **If they told the truth:** The accuser must pick one of the **4 drinks** instead.

One of the four drinks is laced with **poison**. If a player drinks the poison, they are permanently eliminated.<br>
If they survive, the remaining drinks are left on the table—making the risk of drinking the poison even higher for the next unlucky soul.

**The last player left standing wins!**

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

 <p align="center">
  <a href="https://github.com/macchionigiada">
    <img src="https://github.com/macchionigiada.png" width="120px" style="border-radius: 50%; box-shadow: 0 4px 8px rgba(0,0,0,0.1);"/>
    <br />
    <b>macchionigiada</b>
  </a>
  <br />
  <sub>Developer</sub>
</p>


---
