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

    const rodContainer = document.getElementById('rodContainer');
    const addRodBtn = document.getElementById('addRod');

      function createRodCard(rod) {
        const card = document.createElement('div');
        card.className = 'card m-3 p-3 d-flex align-items-center';

        const counter = document.createElement('div');
        counter.className = 'd-flex align-items-center';

        const minus = document.createElement('button');
        minus.className = 'btn btn-outline-secondary btn-sm';
        minus.textContent = '-';

        const count = document.createElement('span');
        count.className = 'mx-2';
        count.textContent = rod.fishCount;

        const plus = document.createElement('button');
        plus.className = 'btn btn-outline-secondary btn-sm';
        plus.textContent = '+';

        counter.appendChild(minus);
        counter.appendChild(count);
        counter.appendChild(plus);

        const timer = document.createElement('span');
        timer.textContent = '00:00';
        timer.style.fontFamily = 'DS-Digital';
        timer.className = 'display-5 flex-grow-1 text-center';

        const del = document.createElement('button');
        del.className = 'btn btn-link text-danger ms-2';
        del.textContent = '🗑️';

        del.addEventListener('click', async () => {
          if (confirm('Supprimer cette canne ?')) {
            await fetch(`/fishing-session/${current.id}/rod/${rod.id}`, {
              method: 'DELETE',
              headers: { sessionId },
            });
            card.remove();
          }
        });

        minus.addEventListener('click', async () => {
          if (parseInt(count.textContent) > 0) {
            const resp = await fetch(`/fishing-session/${current.id}/rod/${rod.id}/fish`, {
              method: 'DELETE',
              headers: { sessionId },
            });
            if (resp.ok) {
              const data = await resp.json();
              count.textContent = data.fishCount;
            }
          }
        });

        plus.addEventListener('click', async () => {
          const resp = await fetch(`/fishing-session/${current.id}/rod/${rod.id}/fish`, {
            method: 'POST',
            headers: { sessionId },
          });
          if (resp.ok) {
            const data = await resp.json();
            count.textContent = data.fishCount;
          }
        });

        card.appendChild(counter);
        card.appendChild(timer);
        card.appendChild(del);
        rodContainer.appendChild(card);
      }

    if (addRodBtn) {
      addRodBtn.addEventListener('click', async () => {
        const resp = await fetch(`/fishing-session/${current.id}/rods`, {
          method: 'POST',
          headers: { sessionId },
        });
        if (resp.ok) {
          const data = await resp.json();
          createRodCard(data);
        }
      });
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
