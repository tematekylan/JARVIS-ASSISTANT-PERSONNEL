import { AppController } from './AppController';
import { TaskManager } from './TaskManager';

export interface AutomationResult {
  executed: boolean;
  actionName?: string;
  responseMessage?: string;
}

export class ActionExecutor {
  private static controller = AppController.getInstance();
  private static taskManager = TaskManager.getInstance();

  /**
   * Evaluates a command string. If an automation pattern is matched, executes it.
   */
  public static execute(rawCommand: string): AutomationResult {
    const cmd = rawCommand.toLowerCase().trim();

    // 1. Create task / reminder
    const taskMatch = cmd.match(/(?:ajoute une tâche|crée une tâche|créer tâche|nouvelle tâche|rappelle-moi de|rappel)\s+(.+)/i);
    if (taskMatch && taskMatch[1]) {
      const taskTitle = taskMatch[1].trim();
      this.taskManager.addTask(taskTitle, 'Rappel', 'HIGH');
      this.controller.notify('Tâche planifiée', taskTitle);
      return {
        executed: true,
        actionName: 'CREATE_TASK',
        responseMessage: `Tâche enregistrée dans votre protocole : "${taskTitle}".`
      };
    }

    // 2. Open Settings / Profile
    if (cmd.includes('paramètre') || cmd.includes('configuration') || cmd.includes('mon profil') || cmd.includes('sécurité')) {
      this.controller.navigateTo('SETTINGS');
      return {
        executed: true,
        actionName: 'NAVIGATE_SETTINGS',
        responseMessage: 'Accès aux paramètres de sécurité et à la configuration du profil.'
      };
    }

    // 3. Command Center / Diagnostics
    if (cmd.includes('diagnostic') || cmd.includes('statut système') || cmd.includes('command center') || cmd.includes('télémétrie')) {
      this.controller.navigateTo('COMMAND_CENTER');
      return {
        executed: true,
        actionName: 'NAVIGATE_COMMAND_CENTER',
        responseMessage: 'Affichage du centre de commandement et de télémétrie quantique.'
      };
    }

    // 4. Tasks screen
    if (cmd.includes('mes tâches') || cmd.includes('liste des tâches') || cmd.includes('voir mes rappels') || cmd.includes('tâches')) {
      this.controller.navigateTo('TASKS');
      return {
        executed: true,
        actionName: 'NAVIGATE_TASKS',
        responseMessage: 'Ouverture du registre des tâches.'
      };
    }

    // 5. Terminal / Cyber Console
    if (cmd.includes('terminal') || cmd.includes('console') || cmd.includes('shell') || cmd.includes('bash')) {
      this.controller.navigateTo('TERMINAL');
      return {
        executed: true,
        actionName: 'NAVIGATE_TERMINAL',
        responseMessage: 'Ouverture de la console cybernétique et du terminal.'
      };
    }

    // 6. Notes / Memory
    if (cmd.includes('mes notes') || cmd.includes('bloc-notes') || cmd.includes('notes')) {
      this.controller.navigateTo('NOTES');
      return {
        executed: true,
        actionName: 'NAVIGATE_NOTES',
        responseMessage: 'Accès au carnet de notes chiffré.'
      };
    }

    // 7. Kotlin Source Code / Android Studio Export
    if (cmd.includes('kotlin') || cmd.includes('code android') || cmd.includes('export mobile')) {
      this.controller.navigateTo('KOTLIN_STUDIO');
      return {
        executed: true,
        actionName: 'NAVIGATE_KOTLIN_STUDIO',
        responseMessage: 'Accès aux sources complètes de l’application Android en 100% Kotlin.'
      };
    }

    return { executed: false };
  }
}
