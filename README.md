# TrackMatch - A Music Recognition System

This repository showcases a full-stack application implementing a proprietary music recognition solution, leveraging audio fingerprinting techniques similar to the **Shazam algorithm**. The system features a modern microservice architecture and asynchronous processing capabilities.

## 🌟 Core Features

| Feature Area | Description | Technologies |
| :--- | :--- | :--- |
| **Architecture** | Clean separation of concerns between client and server. | Next.js, Spring Boot, PostgreSQL |
| **Data Ingestion**| Batch processing for **Spotify (Song, Album, Playlist)** URLs. | Spotify API, YouTube (Audio Source) |
| **Performance** | **Asynchronous and Parallel** execution of I/O-bound tasks (Download & Fingerprinting). | Spring Boot Executors, Async Programming |
| **User Feedback**| **Real-time status reporting** for long-running ingestion processes. | WebSockets |
| **Core Logic** | Extraction of unique **audio fingerprints** and high-speed database matching. | Proprietary Shazam-like Algorithm |

---

## ⚙️ System Architecture & Flow

The backend service is structured around two highly distinct REST endpoints.

### System Architecture Diagram

Below is a high-level overview of the system's architecture and data flow:

![System Architecture Diagram](assets/architecture.png)

### 1. Data Ingestion: Song Registration (`/process-url`)

This endpoint is optimized for high-throughput data loading and persistence, offering live feedback.

* **Input:** A **Spotify URL** (Song, Album, or Playlist).
* **Process Detail:**
    1.  The backend parses the Spotify URL to retrieve all track metadata.
    2.  The corresponding audio source is identified via **YouTube**.
    3.  **Asynchronous/Parallel Execution:** For each song, the system initiates parallel tasks to **download the audio stream** and simultaneously begin the **fingerprint generation** on available audio chunks.
    4.  **Live Feedback (WebSocket):** Progress is relayed to the client via a **WebSocket connection**, providing transparent updates on the status of each song's fingerprinting and database commit.
    5.  Data is persisted across two tables: Song Metadata and the high-volume Fingerprint Hash Table in **PostgreSQL**.

### 2. Query Service: Music Recognition (`/process-audio`)

This endpoint is optimized for ultra-low latency, real-time matching.

* **Input:** A short recorded audio snippet.
* **Process Detail:**
    1.  A fingerprint is generated from the query audio.
    2.  The system performs a rapid, indexed lookup against the **Fingerprint DB** to identify potential matches.
    3.  Match data is validated and correlated with the song metadata, providing the Title, Artist, and time offset.

---

## 🛠️ Technical Stack

| Component | Technology | Role |
| :--- | :--- | :--- |
| **Client** | Next.js (React), **Tailwind CSS** | UI/UX, WebSocket Client. |
| **Server API** | Spring Boot (Java) | REST Controller, Service Layer, Asynchronous Executors. |
| **Database** | PostgreSQL | Robust, indexed storage for all metadata and fingerprints. |
| **External Sources** | Spotify API, YouTube Extractor | Data retrieval and audio sourcing. |

---

## 🚀 Setup and Deployment

### Prerequisites

* Java JDK 17+
* Node.js & npm/yarn
* PostgreSQL Server
* Credentials for **Spotify API** (Client ID/Secret)
* Required audio extraction tool/library integrated into the Spring Boot environment.

### Local Startup

1.  **Database Configuration:** Initialize PostgreSQL and configure the connection details.
2.  **Environment Variables:** Set API keys for Spotify and YouTube.
3.  **Backend:** Build and start the Spring Boot application.
4.  **Frontend:** Navigate to the client directory, install dependencies, and start the Next.js server (`npm run dev`).
