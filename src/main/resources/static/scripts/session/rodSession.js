import { createRodCard } from './rod-card.js';

document.addEventListener('DOMContentLoaded', async () => {
    const addRodBtn = document.getElementById('addRodBtn');
    const rodModal = document.getElementById('createRodModal');
    const rodCancel = document.getElementById('cancelModalBtn');
    const rodConfirm = document.getElementById('confirmModalBtn');
    const rodNameInput = document.getElementById('rodNameInput');

    const logoutBtn = document.getElementById('logoutBtn');

    const sessionNameLabel = document.getElementById('usernameDisplay');

    const sessionId = localStorage.getItem('sessionId');
    const fishingSessionId = localStorage.getItem('currentFishingSessionId');

    if(sessionNameLabel) {
        const sessionName = localStorage.getItem('currentFishingSessionName') || '';
        sessionNameLabel.textContent = sessionName;
    }

    // Charger les cannes existantes
    async function loadRods() {
        if (!fishingSessionId) {
            console.warn('Pas de session de pêche en cours');
            return;
        }

        try {
            const resp = await apiFetch(`/fishing-session/${fishingSessionId}/rods`, {
                headers: { 'sessionId': sessionId }
            });

            if (resp.ok) {
                const rods = await resp.json();
                for (const rod of rods) {
                    await createRodCard(rod);
                }
            }
        } catch (err) {
            console.error('Erreur chargement cannes:', err);
        }
    }

    // Charger les cannes au démarrage
    await loadRods();

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

        try {
            const resp = await apiFetch(`/fishing-session/${fishingSessionId}/rods`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'sessionId': sessionId
                },
                body: JSON.stringify({ name })
            });

            if (resp.ok) {
                const rod = await resp.json();
                rodModal.classList.remove('active');
                await createRodCard(rod);
                showToast('Canne ajoutée avec succès ! 🎣', 'success');
            }
        } catch (err) {
            console.error('Erreur création canne:', err);
            showToast('Erreur lors de la création de la canne', 'danger');
        }
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