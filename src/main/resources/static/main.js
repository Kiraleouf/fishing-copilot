document.addEventListener('DOMContentLoaded', async () => {
  const sessionId = localStorage.getItem('sessionId');

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
    return false;
  }

  async function getCurrentFishingSession() {
    const resp = await apiFetch('/fishing-session/current', { headers: { sessionId } });
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

  const usernameDisplay = document.getElementById('usernameDisplay');
  if (usernameDisplay) {
    usernameDisplay.textContent = localStorage.getItem('login') || '';
  }
  const logoutBtn = document.getElementById('logoutBtn');
  if (logoutBtn) {
    logoutBtn.addEventListener('click', async () => {
      await apiFetch('/fisherman/logout', { headers: { sessionId } });
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
      const resp = await apiFetch('/fishing-session/create', {
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

    const timerDialog = document.getElementById('timerDialog');
    const timerMinutes = document.getElementById('timerMinutes');
    const timerCancel = document.getElementById('timerCancel');
    const timerOk = document.getElementById('timerOk');
    let resolveTimerDialog;
    timerCancel.addEventListener('click', () => {
      timerDialog.classList.add('d-none');
      timerDialog.classList.remove('d-flex');
      if (resolveTimerDialog) resolveTimerDialog(null);
    });
    timerOk.addEventListener('click', () => {
      const minutes = parseInt(timerMinutes.value);
      timerDialog.classList.add('d-none');
      timerDialog.classList.remove('d-flex');
      if (resolveTimerDialog) resolveTimerDialog(minutes);
    });
    function openTimerDialog(initial) {
      if (timerMinutes.options.length === 0) {
        for (let i = 1; i <= 180; i++) {
          const opt = document.createElement('option');
          opt.value = i;
          opt.textContent = i;
          timerMinutes.appendChild(opt);
        }
      }
      timerMinutes.value = initial || 5;
      timerDialog.classList.remove('d-none');
      timerDialog.classList.add('d-flex');
      return new Promise(res => { resolveTimerDialog = res; });
    }

      function createRodCard(rod) {
        const card = document.createElement('div');
        card.className = 'card m-3 p-3 d-flex flex-row align-items-center gap-3';

        const timer = document.createElement('span');
        timer.textContent = '00:00';
        timer.style.fontFamily = 'DS-Digital';
        timer.className = 'display-5';

        const counter = document.createElement('div');
        counter.className = 'd-flex align-items-center bg-white rounded p-1 mx-auto';

        const minus = document.createElement('button');
        minus.className = 'btn btn-outline-secondary btn-sm';
        minus.textContent = '-';

        const count = document.createElement('span');
        count.className = 'mx-2 fw-bold';
        count.textContent = rod.fishCount;

        const plus = document.createElement('button');
        plus.className = 'btn btn-outline-secondary btn-sm';
        plus.textContent = '+';

        counter.appendChild(minus);
        counter.appendChild(count);
        counter.appendChild(plus);

        const del = document.createElement('button');
        del.className = 'btn btn-danger ms-auto';
        del.textContent = '🗑️';

        del.addEventListener('click', async () => {
          if (confirm('Supprimer cette canne ?')) {
            await apiFetch(`/fishing-session/${current.id}/rod/${rod.id}`, {
              method: 'DELETE',
              headers: { sessionId },
            });
            card.remove();
          }
        });

        minus.addEventListener('click', async () => {
          if (parseInt(count.textContent) > 0) {
            const resp = await apiFetch(`/fishing-session/${current.id}/rod/${rod.id}/fish`, {
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
          const resp = await apiFetch(`/fishing-session/${current.id}/rod/${rod.id}/fish`, {
            method: 'POST',
            headers: { sessionId },
          });
          if (resp.ok) {
            const data = await resp.json();
            count.textContent = data.fishCount;
            if (intervalId) {
              clearInterval(intervalId);
              intervalId = null;
            }
            remaining = duration;
            updateTimerDisplay();
            card.classList.remove('bg-success', 'blink', 'border', 'border-danger');
            timer.classList.remove('text-danger');
          }
        });

        let duration = 0;
        let remaining = 0;
        let intervalId = null;

        function updateTimerDisplay() {
          const m = String(Math.floor(remaining / 60)).padStart(2, '0');
          const s = String(remaining % 60).padStart(2, '0');
          timer.textContent = `${m}:${s}`;
        }

        function startCountdown() {
          remaining = duration;
          updateTimerDisplay();
          card.classList.add('bg-success', 'blink');
          card.classList.remove('border-danger');
          timer.classList.remove('text-danger');
          intervalId = setInterval(() => {
            remaining--;
            updateTimerDisplay();
            if (remaining <= 0) {
              clearInterval(intervalId);
              intervalId = null;
              card.classList.remove('bg-success', 'blink');
              card.classList.add('border', 'border-danger');
              timer.classList.add('text-danger');
            }
          }, 1000);
        }

        timer.addEventListener('click', async (e) => {
          e.stopPropagation();
          if (intervalId && !confirm('Changer la durée du timer ?')) {
            return;
          }
          const minutes = await openTimerDialog(duration / 60);
          if (minutes) {
            duration = minutes * 60;
            if (intervalId) clearInterval(intervalId);
            startCountdown();
          }
        });

        card.addEventListener('click', () => {
          if (!intervalId && duration > 0) {
            startCountdown();
          }
        });

        card.appendChild(timer);
        card.appendChild(counter);
        card.appendChild(del);
        rodContainer.appendChild(card);
      }
      const rodsResp = await apiFetch(`/fishing-session/${current.id}/rods`, { headers: { sessionId } });
      if (rodsResp.ok) {
        const rods = await rodsResp.json();
        rods.forEach(createRodCard);
      }

      if (addRodBtn) {
        addRodBtn.addEventListener('click', async () => {
          const resp = await apiFetch(`/fishing-session/${current.id}/rods`, {
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
      await apiFetch('/fishing-session/close', {
        method: 'POST',
        headers: { sessionId },
      });
      localStorage.removeItem('currentFishingSessionId');
      window.location.href = 'home.html';
    });
  }
});
