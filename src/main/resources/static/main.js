document.addEventListener('DOMContentLoaded', async () => {
  const sessionId = localStorage.getItem('sessionId');

  async function validateSession() {
    if (!sessionId) return false;
    const resp = await fetch('/session/check', { headers: { sessionId } });
    if (resp.ok) {
      const username = await resp.text();
      localStorage.setItem('login', username);
      return true;
    }
    localStorage.removeItem('sessionId');
    localStorage.removeItem('login');
    localStorage.removeItem('currentFishingSessionId');
    return false;
  }

  async function getCurrentFishingSession() {
    const resp = await fetch('/fishing-session/current', { headers: { sessionId } });
    if (resp.status === 200) {
      const data = await resp.json();
      localStorage.setItem('currentFishingSessionId', data.id);
      return data;
    }
    localStorage.removeItem('currentFishingSessionId');
    return null;
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
      const login = document.getElementById('login').value;
      const password = document.getElementById('password').value;
      const resp = await fetch('/sign-in', {
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

  const usernameDisplay = document.getElementById('usernameDisplay');
  if (usernameDisplay) {
    usernameDisplay.textContent = localStorage.getItem('login') || '';
  }
  const logoutBtn = document.getElementById('logoutBtn');
  if (logoutBtn) {
    logoutBtn.addEventListener('click', async () => {
      await fetch('/fisherman/logout', { headers: { sessionId } });
      localStorage.removeItem('sessionId');
      localStorage.removeItem('login');
      localStorage.removeItem('currentFishingSessionId');
      window.location.href = 'index.html';
    });
  }

  const startBtn = document.getElementById('startSession');
  if (startBtn) {
    const current = await getCurrentFishingSession();
    if (current) {
      window.location.href = 'session.html';
      return;
    }
    startBtn.addEventListener('click', async () => {
      const name = prompt('Nom de la session', 'sans nom') || 'sans nom';
      const resp = await fetch('/fishing-session/create', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          sessionId: sessionId,
        },
        body: JSON.stringify({ name }),
      });
      if (resp.ok) {
        const data = await resp.json();
        localStorage.setItem('currentFishingSessionId', data.id);
        window.location.href = 'session.html';
      }
    });
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
      await fetch('/fishing-session/close', {
        method: 'POST',
        headers: { sessionId },
      });
      localStorage.removeItem('currentFishingSessionId');
      window.location.href = 'home.html';
    });
  }
});
