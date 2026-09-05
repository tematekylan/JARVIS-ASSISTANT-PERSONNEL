package com.thackman.ai.automation

import android.content.Context
import com.thackman.ai.service.HardwareController

data class AutomationResult(
    val executed: Boolean,
    val actionName: String? = null,
    val responseMessage: String? = null
)

class ActionExecutor(private val context: Context) {
    private val hardware = HardwareController(context)
    private val taskManager = TaskManager.getInstance()

    fun execute(rawCommand: String): AutomationResult {
        val cmd = rawCommand.lowercase().trim()

        // 1. Hardware: Flashlight
        if (cmd.contains("torche") || cmd.contains("lampe")) {
            val enable = !cmd.contains("éteins") && !cmd.contains("stop")
            hardware.toggleFlashlight(enable)
            return AutomationResult(
                executed = true,
                actionName = "HARDWARE_FLASHLIGHT",
                responseMessage = if (enable) "Lampe torche activée." else "Lampe torche désactivée."
            )
        }

        // 2. Hardware: Vibrate / Feedback
        if (cmd.contains("vibre") || cmd.contains("retour haptique")) {
            hardware.vibrate(300)
            return AutomationResult(
                executed = true,
                actionName = "HARDWARE_VIBRATE",
                responseMessage = "Impulsion haptique transmise."
            )
        }

        // 3. Task / Reminder Creation
        val taskRegex = Regex("""(?:ajoute une tâche|crée une tâche|nouvelle tâche|rappelle-moi de|rappel)\s+(.+)""", RegexOption.IGNORE_CASE)
        val match = taskRegex.find(cmd)
        if (match != null) {
            val title = match.groupValues[1].trim()
            taskManager.addTask(title, "Rappel Vocal", "HIGH")
            return AutomationResult(
                executed = true,
                actionName = "CREATE_TASK",
                responseMessage = "Tâche enregistrée dans votre protocole : '$title'."
            )
        }

        return AutomationResult(executed = false)
    }
}
