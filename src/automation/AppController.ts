import { JarvisScreen } from '../types';

export interface AppActionPayload {
  targetScreen?: JarvisScreen;
  contactName?: string;
  callType?: 'audio' | 'video';
  taskTitle?: string;
  messageContent?: string;
}

export class AppController {
  private static instance: AppController;
  private navigateCallback?: (screen: JarvisScreen, payload?: AppActionPayload) => void;
  private triggerCallCallback?: (contactName: string, callType: 'audio' | 'video') => void;
  private showNotificationCallback?: (title: string, message: string) => void;

  private constructor() {}

  public static getInstance(): AppController {
    if (!AppController.instance) {
      AppController.instance = new AppController();
    }
    return AppController.instance;
  }

  public registerNavigation(fn: (screen: JarvisScreen, payload?: AppActionPayload) => void) {
    this.navigateCallback = fn;
  }

  public registerCallTrigger(fn: (contactName: string, callType: 'audio' | 'video') => void) {
    this.triggerCallCallback = fn;
  }

  public registerNotifier(fn: (title: string, message: string) => void) {
    this.showNotificationCallback = fn;
  }

  public navigateTo(screen: JarvisScreen, payload?: AppActionPayload) {
    if (this.navigateCallback) {
      this.navigateCallback(screen, payload);
    }
  }

  public startCall(contactName: string, callType: 'audio' | 'video' = 'audio') {
    if (this.triggerCallCallback) {
      this.triggerCallCallback(contactName, callType);
    }
  }

  public notify(title: string, message: string) {
    if (this.showNotificationCallback) {
      this.showNotificationCallback(title, message);
    }
  }
}
