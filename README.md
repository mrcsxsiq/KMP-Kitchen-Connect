
<img src="assets/header.png">

A **Kotlin Multiplatform (KMP)** application designed to simulate a **Kitchen Display System (KDS)** with communication between devices.

This project enables two roles:
- 🧑‍🍳 **Kitchen Mode** – receives and manages orders in a Kanban-style board
- 📱 **Order Mode** – creates and sends orders to the kitchen

---

## Features

- Kotlin Multiplatform (Desktop, Android, Tablet-ready)
- Peer-to-peer communication using Ktor + JSON
- Real-time order updates
- Kanban-style kitchen display (KDS) 

---
## Demo


https://github.com/user-attachments/assets/b9ac579c-3b54-477f-8a5c-788a8b84b5bb

---

## Architecture Overview

The system is designed to work **without internet**, using local communication between devices.

```text
[ Order Device ]  --->  [ Kitchen Display System ]
        (Client)              (Server)
```

### Key Technologies

- **Kotlin Multiplatform (KMP)**
- **Ktor** for communication
- **Kotlinx Serialization (JSON)**
- **Compose Multiplatform** (UI)
- **SQLite / Local Storage (planned/optional)**

---

## Modules

| Module       | Description                                                     |
|--------------|-----------------------------------------------------------------|
| `composeApp` | Business logic, models, networking                              |
| `androidApp` | Android application                                             |
| `jvmMain`    | Desktop application and Ktor server running inside Kitchen mode |

---

## Data Flow

1. User creates an order on **Order Mode**
2. Order is serialized to JSON
3. Sent via Ktor HTTP (or local network)
4. Kitchen receives and updates Kanban board
5. Status updates can be propagated back

---

## How to Run

### 1. Clone the repository

```bash
git clone https://github.com/mrcsxsiq/KMP-Kitchen-Connect.git
cd KMP-Kitchen-Connect
```

### 2. Run Desktop App (Kitchen Mode)

```bash
./gradlew :desktopApp:run
```

### 3. Run Android App (Order Mode)

Open in Android Studio and run normally.

---

## Communication Strategy

- Uses **Ktor embedded server** in Kitchen Mode
- Order devices act as **clients**
- JSON payloads define orders and updates
- Designed for **LAN environments (same Wi-Fi)**

---

## Future Improvements

- [ ] Authentication between devices
- [ ] Persistent storage (Room / SQLDelight)
- [ ] Logs
- [ ] Notifications
- [ ] Modules

---

## Goal

This project demonstrates:
- Real-world KMP usage
- Device-to-device communication

---

## License

All the code available under the MIT license. See [LICENSE](LICENSE).

```
MIT License

Copyright (c) 2026 Marcos Paulo Farias

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
