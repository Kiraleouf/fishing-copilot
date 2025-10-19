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

    //const closeBtn = document.getElementById('closeSession');

    const closeSessionButton = document.getElementById('closeSession');
    const uploadPhotoModal = document.getElementById('uploadPhotoModal');
    const closeUploadModal = document.getElementById('closeUploadModal');
    const photoInput = document.getElementById('photoInput');
    const photoPreviewContainer = document.getElementById('photoPreviewContainer');
    const uploadPhotosButton = document.getElementById('uploadPhotosButton');
    const finishSessionButton = document.getElementById('finishSessionButton');

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

    // Open the upload photo modal
    closeSessionButton.addEventListener('click', () => {
      uploadPhotoModal.style.display = 'block';
    });

    // Close the upload photo modal
    closeUploadModal.addEventListener('click', () => {
      uploadPhotoModal.style.display = 'none';
    });

    // Preview selected photos
    // Limiter le nombre de photos à 5
    const MAX_PHOTOS = 5;

    photoInput.addEventListener('change', () => {
        if (photoInput.files.length > MAX_PHOTOS) {
            alert(`Vous ne pouvez uploader que ${MAX_PHOTOS} photos à la fois.`);
            photoInput.value = ''; // Réinitialiser l'input
            return;
        }

        photoPreviewContainer.innerHTML = '';
        Array.from(photoInput.files).forEach(file => {
            const reader = new FileReader();
            reader.onload = (e) => {
                const img = document.createElement('img');
                img.src = e.target.result;
                img.style.width = '100px';
                img.style.margin = '5px';
                photoPreviewContainer.appendChild(img);
            };
            reader.readAsDataURL(file);
        });
    });

    uploadPhotosButton.addEventListener('click', async () => {
      const formData = new FormData();
      Array.from(photoInput.files).forEach(file => {
        formData.append('photos', file);
      });

      try {
        const response = await fetch(`/api/fishing-session/${fishingSessionId}/photos`, {
          method: 'POST',
          body: formData
        });

        if (response.ok) {
          alert('Photos uploadées avec succès !');
        } else {
          alert('Échec de l\'upload des photos.');
        }
      } catch (error) {
        console.error('Erreur lors de l\'upload :', error);
        alert('Une erreur est survenue.');
      }
    });

    // Finish session after uploading photos
    finishSessionButton.addEventListener('click', async () => {
      await apiFetch('/fishing-session/close', {
                    method: 'POST',
                    headers: { sessionId },
                  });
                    localStorage.removeItem('currentFishingSessionId');
                    localStorage.removeItem('currentFishingSessionName');
                  window.location.href = 'home.html';
    });
});