# T-HACKMAN AI // Application Android Native Jetpack Compose

**T-HACKMAN AI** est une application mobile Android native futuriste d'assistant personnel cybernétique, propulsée par un noyau holographique animé en Jetpack Compose, des commandes vocales bidirectionnelles (SpeechRecognizer & TextToSpeech), le SDK Google Generative AI (Gemini), et l'intégration des contrôles matériels réels (torche, retours haptiques, télémétrie batterie).

---

## ⚡ Fonctionnalités Clés

- 🔮 **Cœur Holographique Animé (AICoreOrb)** : Rendu réactif 60 FPS avec animations infinies de rotation, pulsation du réacteur cybernétique et réactivité visuelle à l'amplitude audio.
- 🧠 **Moteur IA Gemini (Google Generative AI SDK)** : Dialogue intelligent propulsé par Gemini 2.5 Flash avec persona cybernétique et mémoire contextuelle.
- 🎙️ **Interaction Vocale Native & Synthèse Vocale** :
  - Reconnaissance vocale par reconnaissance de parole Android native (`SpeechRecognizer`).
  - Synthèse vocale fluide (`TextToSpeech`) avec retour audio cybernétique.
- 🛠️ **Centre de Contrôle & Matériel Réel** :
  - Contrôle réel de la lampe torche via `CameraManager`.
  - Retour haptique / vibrations via le service de vibration Android.
  - Télémétrie en temps réel de la batterie via `BatteryManager`.
- 📋 **Gestionnaire de Protocoles & Tâches** :
  - Création de directives et rappels avec priorisation (HIGH, MEDIUM, LOW).
  - Déclenchement automatique par commande vocale (ex. : *"rappelle-moi de...", "ajoute une tâche..."*).
  - Contrôle d'état et suppression interactive.

---

## 🛠️ Architecture & Technologies

- **Langage** : Kotlin
- **Interface Utilisateur** : Jetpack Compose & Material 3
- **Architecture** : MVVM (Model-View-ViewModel) avec Kotlin Coroutines et StateFlow
- **IA** : Google Generative AI Client (`com.google.ai.client.generativeai:generativeai:0.9.0`)
- **Services Matériels** : Camera2 API, VibratorManager / Vibrator, BatteryManager, SpeechRecognizer, TextToSpeech
- **Build System** : Gradle Kotlin DSL (`build.gradle.kts`)
