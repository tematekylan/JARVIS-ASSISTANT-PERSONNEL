package com.example.ui.components

/**
 * Assistant states specified in the T-HACKMAN AI specification.
 * Each state modifies speed, pulse rate, rings, particles, waveforms, text, and visual telemetry.
 */
enum class AssistantState {
    IDLE,
    LISTENING,
    THINKING,
    PROCESSING,
    SPEAKING,
    SUCCESS,
    ERROR;

    val label: String
        get() = when (this) {
            IDLE -> "SYSTEM READY"
            LISTENING -> "LISTENING"
            THINKING -> "THINKING..."
            PROCESSING -> "PROCESSING"
            SPEAKING -> "SPEAKING"
            SUCCESS -> "TASK COMPLETE"
            ERROR -> "SYSTEM ERROR"
        }

    val subtitle: String
        get() = when (this) {
            IDLE -> "En attente de votre commande"
            LISTENING -> "Microphone actif • Je vous écoute..."
            THINKING -> "Analyse neuronale de la requête en cours..."
            PROCESSING -> "Exécution de la tâche système..."
            SPEAKING -> "Transmission vocale & synthèse active"
            SUCCESS -> "Opération accomplie avec succès"
            ERROR -> "Impossible d'exécuter la requête"
        }
}

// Extension to map backward-compatible JarvisCoreState to AssistantState
fun JarvisCoreState.toAssistantState(): AssistantState = when (this) {
    JarvisCoreState.IDLE -> AssistantState.IDLE
    JarvisCoreState.LISTENING -> AssistantState.LISTENING
    JarvisCoreState.THINKING -> AssistantState.THINKING
    JarvisCoreState.SPEAKING -> AssistantState.SPEAKING
    JarvisCoreState.ERROR -> AssistantState.ERROR
}
