import { createRodCard } from './rod-card.js';

document.addEventListener('DOMContentLoaded', async () => {
    const addRodBtn = document.getElementById('addRodBtn');
    const rodModal = document.getElementById('createRodModal');
    const rodCancel = document.getElementById('cancelModalBtn');
    const rodConfirm = document.getElementById('confirmModalBtn');
    const rodNameInput = document.getElementById('rodNameInput');

    const logoutBtn = document.getElementById('logoutBtn');

    const sessionNameLabel = document.getElementById('usernameDisplay');

    if(sessionNameLabel) {
        const sessionName = localStorage.getItem('currentFishingSessionName') || '';
        sessionNameLabel.textContent = sessionName;
    }

    if (addRodBtn) {
      addRodBtn.addEventListener('click', () => {
        rodModal.classList.add('active');
        rodNameInput.value = '';
        rodNameInput.focus();
      });
    }

    if (rodCancel) {
      rodCancel.addEventListener('click', () => {
        rodModal.classList.remove('active');
      });
    }

    if (rodConfirm) {
      rodConfirm.addEventListener('click', async () => {
        const name = rodNameInput.value.trim() || 'sans nom';
        console.log('Création canne:', name);
        // 👉 appel à ton backend ici si besoin
        rodModal.classList.remove('active');
        await createRodCard(name);
      });
    }

    if (logoutBtn) {
        logoutBtn.addEventListener('click', async () => {
            await apiFetch('/fisherman/logout', { headers: { sessionId } });
            localStorage.removeItem('sessionId');
            localStorage.removeItem('login');
            localStorage.removeItem('currentFishingSessionId');
            localStorage.removeItem('currentFishingSessionName');
            window.location.href = 'index.html';
        });
    }
});