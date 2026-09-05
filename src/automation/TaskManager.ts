import { TaskItem } from '../types';

export class TaskManager {
  private static instance: TaskManager;
  private tasks: TaskItem[] = [];
  private listeners: ((tasks: TaskItem[]) => void)[] = [];

  private constructor() {
    try {
      const stored = localStorage.getItem('thack_tasks');
      if (stored) {
        this.tasks = JSON.parse(stored);
      } else {
        this.tasks = [
          {
            id: 't-1',
            title: 'Initialisation du protocole T-HACKMAN AI',
            time: 'Aujourd’hui',
            category: 'Système',
            isCompleted: true,
            priority: 'HIGH'
          },
          {
            id: 't-2',
            title: 'Synchronisation Firebase Cloud Firestore',
            time: 'En continu',
            category: 'Analyse',
            isCompleted: false,
            priority: 'HIGH'
          }
        ];
      }
    } catch {
      // fallback
    }
  }

  public static getInstance(): TaskManager {
    if (!TaskManager.instance) {
      TaskManager.instance = new TaskManager();
    }
    return TaskManager.instance;
  }

  public getTasks(): TaskItem[] {
    return [...this.tasks];
  }

  public addTask(title: string, category: string = 'Rappel', priority: 'HIGH' | 'MEDIUM' | 'LOW' = 'MEDIUM'): TaskItem {
    const newTask: TaskItem = {
      id: 'task_' + Date.now(),
      title,
      time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
      category,
      isCompleted: false,
      priority,
      createdAt: Date.now()
    };
    this.tasks.unshift(newTask);
    this.persist();
    this.notify();
    return newTask;
  }

  public toggleTask(id: string) {
    this.tasks = this.tasks.map(t => t.id === id ? { ...t, isCompleted: !t.isCompleted } : t);
    this.persist();
    this.notify();
  }

  public removeTask(id: string) {
    this.tasks = this.tasks.filter(t => t.id !== id);
    this.persist();
    this.notify();
  }

  public subscribe(listener: (tasks: TaskItem[]) => void): () => void {
    this.listeners.push(listener);
    listener(this.tasks);
    return () => {
      this.listeners = this.listeners.filter(l => l !== listener);
    };
  }

  private persist() {
    try {
      localStorage.setItem('thack_tasks', JSON.stringify(this.tasks));
    } catch {
      // ignore
    }
  }

  private notify() {
    this.listeners.forEach(l => l([...this.tasks]));
  }
}
