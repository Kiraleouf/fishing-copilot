document.addEventListener('DOMContentLoaded', () => {
  const form = document.getElementById('loginForm');
  if (form) {
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
        window.location.href = 'home.html';
      }
    });
    const reg = document.getElementById('openRegister');
    if (reg) {
      reg.addEventListener('click', () => {
        window.location.href = 'register.html';
      });
    }
  }

  const startBtn = document.getElementById('startSession');
  if (startBtn) {
    startBtn.addEventListener('click', async () => {
      const name = prompt('Nom de la session', 'sans nom') || 'sans nom';
      const sessionId = localStorage.getItem('sessionId');
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
  }
});
