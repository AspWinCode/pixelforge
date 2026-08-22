// Обёртка над официальным postMessage API Snap!.

function callSnapApi<T>(iframeWindow: Window, selector: string, params: unknown[] = [], timeoutMs = 5000): Promise<T> {
  return new Promise((resolve, reject) => {
    const timeout = setTimeout(() => {
      window.removeEventListener('message', handler);
      reject(new Error(`Snap! API timeout: ${selector}`));
    }, timeoutMs);

    function handler(e: MessageEvent) {
      if (e.data && e.data.selector === selector) {
        clearTimeout(timeout);
        window.removeEventListener('message', handler);
        resolve(e.data.response as T);
      }
    }

    window.addEventListener('message', handler);
    iframeWindow.postMessage({ selector, params }, '*');
  });
}

export function getProjectXML(iframeWindow: Window): Promise<string> {
  return callSnapApi<string>(iframeWindow, 'getProjectXML');
}

export function loadProjectXML(iframeWindow: Window, xml: string): Promise<void> {
  return callSnapApi<void>(iframeWindow, 'loadProjectXML', [xml]);
}

// Официальный метод Snap!: true, если в проекте есть изменения с момента
// последнего сохранения. Используем это, чтобы автосохранение не заливало
// в S3 одинаковый файл заново, если ученик просто оставил вкладку открытой
// и ничего не делал.
export function hasUnsavedChanges(iframeWindow: Window): Promise<boolean> {
  return callSnapApi<boolean>(iframeWindow, 'unsavedChanges');
}

export async function waitForSnapReady(iframeWindow: Window, maxAttempts = 20): Promise<void> {
  for (let attempt = 0; attempt < maxAttempts; attempt++) {
    try {
      await callSnapApi<string>(iframeWindow, 'getProjectXML', [], 500);
      return;
    } catch {
      // не ответил за 500мс, пробуем ещё раз
    }
  }
  throw new Error('Snap! не ответил за отведённое время');
}
