document.addEventListener('DOMContentLoaded', async () => {

    const sessionId = localStorage.getItem('sessionId');
    const logoutBtn = document.getElementById('logoutBtn');
    const startBtn = document.getElementById('startSession');
    const userNameLabel = document.getElementById('usernameDisplay');

    //Modale elements
    const modal = document.getElementById('createSessionModal');
    const confirmBtn = document.getElementById('confirmModalBtn');
    const cancelBtn = document.getElementById('cancelModalBtn');
    const sessionNameInput = document.getElementById('sessionNameInput');

    let currentPage = 0;
    let loading = false;

    const sessionList = document.getElementById('sessionList');

    const username = localStorage.getItem('login') || 'Utilisateur';
    if(userNameLabel) {
        userNameLabel.textContent = username;
    }

    if (logoutBtn) {
        logoutBtn.addEventListener('click', async () => {
          await apiFetch('/fisherman/logout', { headers: { sessionId } });
          localStorage.removeItem('sessionId');
          localStorage.removeItem('login');
          localStorage.removeItem('currentFishingSessionId');
          window.location.href = 'index.html';
        });
    }

    if (startBtn) {
      const current = await getCurrentFishingSession();
      if (current) {
        window.location.href = 'session.html';
        return;
      }

      startBtn.addEventListener('click', () => {
        sessionNameInput.value = '';
        modal.classList.add('active');
        sessionNameInput.focus();
      });

      cancelBtn.addEventListener('click', () => {
        modal.classList.remove('active');
      });

      confirmBtn.addEventListener('click', async () => {
        const name = sessionNameInput.value.trim() || 'sans nom';
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
          modal.classList.remove('active');
          window.location.href = 'session.html';
        }
      });
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

    const SESSION_HEADER = 'sessionId';

    function getSessionIdOrRedirect() {
      const sid = localStorage.getItem(SESSION_HEADER);
      if (!sid) {
        console.warn('Aucun session-id trouvé, redirection vers /login.html');
        window.location.href = '/login.html';
        throw new Error('Missing session-id');
      }
      return sid;
    }

    async function fetchSessions(page = 0) {
      loading = true;

      const sessionId = getSessionIdOrRedirect();
      const url = `/fishing-session/history?page=${page}&size=50`;

      const response = await fetch(url, {
        method: 'GET',
        headers: {
          'Accept': 'application/json',
          [SESSION_HEADER]: sessionId
        }
      });

      if (!response.ok) {
        console.error('Erreur fetch sessions:', response.status, await response.text());
        loading = false;
        return false;
      }

      const data = await response.json();

      data.items.forEach(sess => {
        const el = document.createElement('div');
        el.className = 'session-item';
        el.textContent = `${sess.date} — ${sess.name}`;
        el.addEventListener('click', () => console.log('Clicked session', sess.id));
        sessionList.appendChild(el);
      });

      currentPage = data.page;
      loading = false;
      return data.hasNext;
    }

    async function init() {
      await fetchSessions();

      sessionList.addEventListener('scroll', async () => {
        if (loading) return;
        const nearBottom = sessionList.scrollTop + sessionList.clientHeight >= sessionList.scrollHeight - 100;
        if (nearBottom) {
          const hasNext = await fetchSessions(currentPage + 1);
          if (!hasNext) {
            console.log("Fin de l'historique 🎣");
          }
        }
      });

      document.getElementById('startSession').addEventListener('click', () => {
        console.log('Créer une nouvelle session');
      });
    }

    init()
});