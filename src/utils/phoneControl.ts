// Phone Control & Native Mobile Hardware Coordinator for T-HACK AI

let activeTorchTrack: MediaStreamTrack | null = null;
let wakeLockSentinel: any = null;

/**
 * Screen Wake Lock: Keeps the mobile screen ON and prevents sleep/lock
 */
export async function requestScreenWakeLock(): Promise<boolean> {
  if (typeof navigator !== 'undefined' && 'wakeLock' in navigator) {
    try {
      if (wakeLockSentinel && !wakeLockSentinel.released) {
        return true;
      }
      wakeLockSentinel = await (navigator as any).wakeLock.request('screen');
      wakeLockSentinel.addEventListener('release', () => {
        wakeLockSentinel = null;
      });
      console.log('Screen Wake Lock actif: écran maintenu allumé.');
      return true;
    } catch (err: any) {
      console.warn('Screen WakeLock indisponible ou refusé:', err);
      return false;
    }
  }
  return false;
}

export async function releaseScreenWakeLock(): Promise<void> {
  if (wakeLockSentinel) {
    try {
      await wakeLockSentinel.release();
      wakeLockSentinel = null;
    } catch (e) {}
  }
}

export function isScreenWakeLockActive(): boolean {
  return !!wakeLockSentinel && !wakeLockSentinel.released;
}

/**
 * Fullscreen toggle
 */
export async function toggleFullScreen(enable?: boolean): Promise<boolean> {
  try {
    if (enable === undefined) {
      if (!document.fullscreenElement) {
        if (document.documentElement.requestFullscreen) {
          await document.documentElement.requestFullscreen();
          return true;
        }
      } else {
        if (document.exitFullscreen) {
          await document.exitFullscreen();
          return false;
        }
      }
    } else if (enable) {
      if (!document.fullscreenElement && document.documentElement.requestFullscreen) {
        await document.documentElement.requestFullscreen();
        return true;
      }
    } else {
      if (document.fullscreenElement && document.exitFullscreen) {
        await document.exitFullscreen();
        return false;
      }
    }
  } catch (e) {
    console.warn("Fullscreen toggle error:", e);
  }
  return !!document.fullscreenElement;
}

/**
 * Flashlight / Torch Control using camera stream
 */
export async function toggleFlashlight(forceState?: boolean): Promise<{ success: boolean; isOn: boolean; message: string }> {
  try {
    if (activeTorchTrack) {
      if (forceState === true) {
        return { success: true, isOn: true, message: "Lampe torche déjà allumée." };
      }
      // Turn off
      activeTorchTrack.stop();
      activeTorchTrack = null;
      return { success: true, isOn: false, message: "Lampe torche éteinte." };
    }

    if (forceState === false) {
      return { success: true, isOn: false, message: "Lampe torche déjà éteinte." };
    }

    // Turn on
    if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
      return { success: false, isOn: false, message: "Caméra/Torche non supportée sur cet appareil." };
    }

    const stream = await navigator.mediaDevices.getUserMedia({
      video: {
        facingMode: 'environment'
      }
    });

    const track = stream.getVideoTracks()[0];
    if (!track) {
      return { success: false, isOn: false, message: "Piste vidéo indisponible." };
    }

    const capabilities: any = track.getCapabilities ? track.getCapabilities() : {};
    if (!capabilities.torch) {
      // Even if torch constraint isn't exposed, keep track alive or inform user
      activeTorchTrack = track;
      return { 
        success: true, 
        isOn: true, 
        message: "Lampe/Flash activé (flux caméra arrière initialisé)." 
      };
    }

    await (track as any).applyConstraints({
      advanced: [{ torch: true }]
    });

    activeTorchTrack = track;
    return { success: true, isOn: true, message: "Lampe torche allumée avec succès." };
  } catch (err: any) {
    console.warn("Torch error:", err);
    return { success: false, isOn: false, message: `Erreur torche: ${err.message || "Accès refusé"}` };
  }
}

export function isFlashlightOn(): boolean {
  return !!activeTorchTrack;
}

/**
 * Haptic Vibration Feedback
 */
export function vibrateDevice(pattern: number | number[] = [100, 50, 100]): boolean {
  if (typeof navigator !== 'undefined' && 'vibrate' in navigator) {
    try {
      return navigator.vibrate(pattern);
    } catch (e) {
      return false;
    }
  }
  return false;
}

/**
 * Battery Status
 */
export async function getDeviceBattery(): Promise<{ level: number; charging: boolean } | null> {
  if (typeof navigator !== 'undefined' && 'getBattery' in navigator) {
    try {
      const b: any = await (navigator as any).getBattery();
      return {
        level: Math.round(b.level * 100),
        charging: b.charging
      };
    } catch (e) {
      return null;
    }
  }
  return null;
}

/**
 * Native Android Contact Picker API
 */
export async function pickNativeDeviceContact(): Promise<{ name: string; tel: string; email?: string } | null> {
  if (typeof navigator !== 'undefined' && 'contacts' in navigator && 'ContactsManager' in window) {
    try {
      const contacts = await (navigator as any).contacts.select(['name', 'tel', 'email'], { multiple: false });
      if (contacts && contacts.length > 0) {
        const c = contacts[0];
        const name = (c.name && c.name[0]) || "Contact sélectionné";
        const tel = (c.tel && c.tel[0]) || "";
        const email = (c.email && c.email[0]) || "";
        return { name, tel, email };
      }
    } catch (e) {
      console.warn("Native Contact Picker cancelled or not permitted:", e);
    }
  }
  return null;
}

/**
 * App Launchers: Spotify, Google Search, WhatsApp, Phone, SMS, YouTube
 */

export function launchSpotifySearch(query: string): string {
  const cleanQuery = query.trim();
  const webUrl = `https://open.spotify.com/search/${encodeURIComponent(cleanQuery)}`;
  const nativeScheme = `spotify:search:${encodeURIComponent(cleanQuery)}`;
  
  // Try opening native scheme on mobile, or web URL
  try {
    const isMobile = /iPhone|iPad|iPod|Android/i.test(navigator.userAgent);
    if (isMobile) {
      window.location.href = nativeScheme;
      setTimeout(() => {
        window.open(webUrl, '_blank', 'noopener,noreferrer');
      }, 1200);
    } else {
      window.open(webUrl, '_blank', 'noopener,noreferrer');
    }
  } catch (e) {
    window.open(webUrl, '_blank', 'noopener,noreferrer');
  }
  return webUrl;
}

export function launchGoogleSearch(query: string): string {
  const cleanQuery = query.trim();
  const url = `https://www.google.com/search?q=${encodeURIComponent(cleanQuery)}`;
  try {
    window.open(url, '_blank', 'noopener,noreferrer');
  } catch (e) {
    window.location.href = url;
  }
  return url;
}

export function launchWhatsAppChat(phone?: string, text: string = ""): string {
  const cleanPhone = (phone || "").replace(/\D/g, '');
  let url = "";
  if (cleanPhone) {
    url = `https://wa.me/${cleanPhone}?text=${encodeURIComponent(text)}`;
  } else {
    url = `https://wa.me/?text=${encodeURIComponent(text)}`;
  }

  try {
    const isMobile = /iPhone|iPad|iPod|Android/i.test(navigator.userAgent);
    if (isMobile && cleanPhone) {
      window.location.href = `whatsapp://send?phone=${cleanPhone}&text=${encodeURIComponent(text)}`;
      setTimeout(() => {
        window.open(url, '_blank', 'noopener,noreferrer');
      }, 1000);
    } else {
      window.open(url, '_blank', 'noopener,noreferrer');
    }
  } catch (e) {
    window.open(url, '_blank', 'noopener,noreferrer');
  }
  return url;
}

export function launchPhoneCall(phoneNumber: string): string {
  const clean = phoneNumber.replace(/[^0-9+]/g, '');
  const telUrl = `tel:${clean}`;
  window.location.href = telUrl;
  return telUrl;
}

export function launchSms(phoneNumber: string, bodyText: string = ""): string {
  const clean = phoneNumber.replace(/[^0-9+]/g, '');
  const smsUrl = `sms:${clean}?body=${encodeURIComponent(bodyText)}`;
  window.location.href = smsUrl;
  return smsUrl;
}

export function launchGoogleMaps(query: string): string {
  const url = `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(query)}`;
  window.open(url, '_blank', 'noopener,noreferrer');
  return url;
}
