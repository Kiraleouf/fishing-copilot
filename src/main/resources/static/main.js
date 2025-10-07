document.addEventListener('DOMContentLoaded', async () => {
  const sessionId = localStorage.getItem('sessionId');
  const dingSound = new Audio('sound/ding.mp3');

  function unlockSound() {
    dingSound.play().then(() => {
      dingSound.pause();
      dingSound.currentTime = 0;
    }).catch(() => {});
  }
  document.addEventListener('click', unlockSound, { once: true });
  document.addEventListener('touchstart', unlockSound, { once: true });

  async function validateSession() {
    if (!sessionId) return false;
    const resp = await apiFetch('/session/check', { headers: { sessionId } });
    if (resp.ok) {
      const username = await resp.text();
      localStorage.setItem('login', username);
      return true;
    }
    localStorage.removeItem('sessionId');
    localStorage.removeItem('login');
    localStorage.removeItem('currentFishingSessionId');
    localStorage.removeItem('currentFishingSessionName');
    return false;
  }

  const form = document.getElementById('loginForm');
  if (form) {
    if (sessionId && await validateSession()) {
      const current = await getCurrentFishingSession();
      window.location.href = current ? 'session.html' : 'home.html';
      return;
    }
    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      const login = document.getElementById('username').value;
      const password = document.getElementById('password').value;
      const resp = await apiFetch('/sign-in', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ login, password }),
      });
      if (resp.ok) {
        const data = await resp.json();
        localStorage.setItem('sessionId', data.sessionId);
        localStorage.setItem('login', login);
        window.location.href = 'home.html';
      }
    });
    const reg = document.getElementById('openRegister');
    if (reg) {
      reg.addEventListener('click', () => {
        window.location.href = 'register.html';
      });
    }
    return;
  }

  if (!await validateSession()) {
    window.location.href = 'index.html';
    return;
  }

  const closeBtn = document.getElementById('closeSession');
  if (closeBtn) {
    const current = await getCurrentFishingSession();
    if (!current) {
      window.location.href = 'home.html';
      return;
    }

    closeBtn.addEventListener('click', async () => {
      await apiFetch('/fishing-session/close', {
        method: 'POST',
        headers: { sessionId },
      });
        localStorage.removeItem('currentFishingSessionId');
        localStorage.removeItem('currentFishingSessionName');
      window.location.href = 'home.html';
    });
  }


    async function getCurrentFishingSession() {
        const resp = await apiFetch('/fishing-session/current', { headers: { sessionId } });
        if (resp.status === 200) {
          const data = await resp.json();
          localStorage.setItem('currentFishingSessionId', data.id);
          localStorage.setItem('currentFishingSessionName', data.name);
          return data;
        }
        localStorage.removeItem('currentFishingSessionId');
        localStorage.removeItem('currentFishingSessionName');
        return null;
    }
});
