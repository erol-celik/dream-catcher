import { LocalNotifications } from '@capacitor/local-notifications';

class NotificationManager {
  constructor() {
    this.listenersSetup = false;
  }

  async requestPermissions() {
    const permissionStatus = await LocalNotifications.requestPermissions();

    if (permissionStatus.display === 'granted') {
      await this.scheduleNightlyAlarm();
      await this.setupListeners();
    }
  }

  async scheduleNightlyAlarm() {
    await LocalNotifications.cancel({
      notifications: [{ id: 1 }]
    });

    await LocalNotifications.schedule({
      notifications: [
        {
          id: 1,
          title: '🌙 Dream Catcher',
          body: 'Rüya görmeye hazır mısın?Alarmını kurmayı untuma!',
          schedule: {
            on: { hour: 23, minute: 0 },
            repeats: true,
            allowWhileIdle: true
          }
        }
      ]
    });
  }

  async setupListeners() {
    if (this.listenersSetup) {
      return;
    }

    await LocalNotifications.addListener('localNotificationActionPerformed', () => {
      alert('System Alarm App will open here...');
    });

    this.listenersSetup = true;
  }
}

export const notificationService = new NotificationManager();
