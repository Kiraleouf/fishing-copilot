export async function createRodCard(rodData) {
  const response = await fetch('/templates/rod-card.html');
  const html = await response.text();

  const wrapper = document.createElement('div');
  wrapper.innerHTML = html;
  const card = wrapper.firstElementChild;

  // Données de la carte
  const rodId = rodData.id;
  const rodName = rodData.name || "Canne 1";
  const fishCount = rodData.fishCount || 0;
  const fishingSessionId = localStorage.getItem('currentFishingSessionId');
  const sessionId = localStorage.getItem('sessionId');

  card.dataset.rodId = rodId;
  card.querySelector('.rod-title').textContent = rodName;

  // Éléments DOM
  const timerDisplay = card.querySelector('.timer-display');
  const playBtn = card.querySelector('.rod-play-btn');
  const counterValue = card.querySelector('.counter-value');
  const minusBtn = card.querySelector('.counter-btn:first-child');
  const plusBtn = card.querySelector('.counter-btn:last-child');
  const closeBtn = card.querySelector('.rod-close-btn');

  // Éléments du time picker
  const timePickerModal = card.querySelector('.time-picker-modal');
  const timePickerOverlay = card.querySelector('.time-picker-overlay');
  const timePickerWheel = card.querySelector('.time-picker-wheel');
  const timePickerCancel = card.querySelector('.time-picker-cancel');
  const timePickerConfirm = card.querySelector('.time-picker-confirm');

  // État du timer
  let timerSeconds = 0;
  let timerInterval = null;
  let isPlaying = false;
  let isPaused = false;
  const dingSound = new Audio('/sound/ding.mp3');

  // Initialiser le compteur
  counterValue.textContent = fishCount;

  // ===== Time Picker Wheel =====
  function initializeTimePicker() {
    // Créer les éléments de 0 à 240 minutes
    for (let i = 0; i <= 240; i++) {
      const item = document.createElement('div');
      item.className = 'time-picker-item';
      item.textContent = `${i} min`;
      item.dataset.value = i;
      timePickerWheel.appendChild(item);
    }
  }

  function updateWheelSelection() {
    const items = timePickerWheel.querySelectorAll('.time-picker-item');
    const container = timePickerWheel;
    const containerRect = container.getBoundingClientRect();
    const centerY = containerRect.top + containerRect.height / 2;

    let closestItem = null;
    let closestDistance = Infinity;

    items.forEach(item => {
      const rect = item.getBoundingClientRect();
      const itemCenterY = rect.top + rect.height / 2;
      const distance = Math.abs(centerY - itemCenterY);

      // Enlever toutes les classes
      item.classList.remove('selected', 'near');

      // Trouver l'élément le plus proche du centre
      if (distance < closestDistance) {
        closestDistance = distance;
        closestItem = item;
      }

      // Ajouter la classe "near" si proche
      if (distance < 60) {
        item.classList.add('near');
      }
    });

    // Marquer l'élément central comme sélectionné
    if (closestItem) {
      closestItem.classList.add('selected');
    }

    return closestItem ? parseInt(closestItem.dataset.value) : 0;
  }

  function scrollToMinute(minute) {
    const items = timePickerWheel.querySelectorAll('.time-picker-item');
    const targetItem = Array.from(items).find(item => parseInt(item.dataset.value) === minute);
    if (targetItem) {
      const container = timePickerWheel;
      const containerRect = container.getBoundingClientRect();
      const itemRect = targetItem.getBoundingClientRect();
      const scrollOffset = itemRect.top - containerRect.top - containerRect.height / 2 + itemRect.height / 2;
      container.scrollBy({ top: scrollOffset, behavior: 'smooth' });
    }
  }

  function openTimePicker() {
    timePickerModal.classList.add('active');
    // Scroller à la valeur actuelle
    const currentMinutes = Math.floor(timerSeconds / 60);
    setTimeout(() => scrollToMinute(currentMinutes), 50);
  }

  function closeTimePicker() {
    timePickerModal.classList.remove('active');
  }

  // Initialiser le wheel picker
  initializeTimePicker();

  // Événements du time picker
  let scrollTimeout;
  timePickerWheel.addEventListener('scroll', () => {
    clearTimeout(scrollTimeout);
    scrollTimeout = setTimeout(() => {
      const selectedValue = updateWheelSelection();
      // Snap to the selected item
      const items = timePickerWheel.querySelectorAll('.time-picker-item');
      const selectedItem = Array.from(items).find(item => parseInt(item.dataset.value) === selectedValue);
      if (selectedItem) {
        const container = timePickerWheel;
        const containerRect = container.getBoundingClientRect();
        const itemRect = selectedItem.getBoundingClientRect();
        const scrollOffset = itemRect.top - containerRect.top - containerRect.height / 2 + itemRect.height / 2;
        container.scrollBy({ top: scrollOffset, behavior: 'smooth' });
      }
    }, 100);
    updateWheelSelection();
  });

  timePickerOverlay.addEventListener('click', closeTimePicker);
  timePickerCancel.addEventListener('click', closeTimePicker);

  timePickerConfirm.addEventListener('click', () => {
    const selectedMinutes = updateWheelSelection();
    timerSeconds = selectedMinutes * 60;
    updateTimerDisplay();
    setCardState('');
    closeTimePicker();
  });

  // Formater le temps MM:SS
  function formatTime(seconds) {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${String(mins).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;
  }

  // Mettre à jour l'affichage du timer
  function updateTimerDisplay() {
    timerDisplay.textContent = formatTime(timerSeconds);
  }

  // Changer l'état de la carte
  function setCardState(state) {
    card.classList.remove('rod-card-active', 'rod-card-inactive');
    if (state === 'active') {
      card.classList.add('rod-card-active');
    } else if (state === 'inactive') {
      card.classList.add('rod-card-inactive');
    }
  }

  // Mettre à jour l'icône du bouton play/pause
  function updatePlayButton() {
    const svg = playBtn.querySelector('svg');
    if (isPlaying && !isPaused) {
      // Icône pause (deux barres)
      svg.innerHTML = '<rect x="30" y="25" width="12" height="50" fill="white"/><rect x="58" y="25" width="12" height="50" fill="white"/>';
    } else {
      // Icône play (triangle)
      svg.innerHTML = '<polygon points="35,25 75,50 35,75" fill="white" />';
    }
  }

  // Démarrer le décompte
  function startTimer() {
    if (timerSeconds <= 0) return;

    isPlaying = true;
    isPaused = false;
    setCardState('active');
    updatePlayButton();

    timerInterval = setInterval(() => {
      timerSeconds--;
      updateTimerDisplay();

      if (timerSeconds <= 0) {
        stopTimer();
        setCardState('inactive');
        dingSound.play().catch(err => console.warn('Erreur lecture son:', err));
      }
    }, 1000);
  }

  // Mettre en pause
  function pauseTimer() {
    isPaused = true;
    clearInterval(timerInterval);
    timerInterval = null;
    setCardState('');
    updatePlayButton();
  }

  // Arrêter le timer
  function stopTimer() {
    isPlaying = false;
    isPaused = false;
    clearInterval(timerInterval);
    timerInterval = null;
    updatePlayButton();
  }

  // Ouvrir le sélecteur de temps au clic sur le timer
  timerDisplay.addEventListener('click', () => {
    if (isPlaying) return; // Ne pas permettre le changement pendant le décompte
    openTimePicker();
  });

  // Bouton Play/Pause
  playBtn.addEventListener('click', () => {
    if (!isPlaying) {
      // Démarrer
      startTimer();
    } else if (!isPaused) {
      // Mettre en pause
      pauseTimer();
    } else {
      // Reprendre
      startTimer();
    }
  });

  // Bouton + (ajouter un poisson)
  plusBtn.addEventListener('click', async () => {
    try {
      const resp = await apiFetch(`/fishing-session/${fishingSessionId}/rod/${rodId}/fish`, {
        method: 'POST',
        headers: {
          'sessionId': sessionId
        }
      });

      if (resp.ok) {
        const data = await resp.json();
        counterValue.textContent = data.fishCount;
        showToast('Poisson ajouté ! 🐟', 'success');
      }
    } catch (err) {
      console.error('Erreur ajout poisson:', err);
      showToast('Erreur lors de l\'ajout du poisson', 'danger');
    }
  });

  // Bouton - (retirer un poisson)
  minusBtn.addEventListener('click', async () => {
    const currentCount = parseInt(counterValue.textContent);
    if (currentCount <= 0) {
      showToast('Aucun poisson à retirer', 'warning');
      return;
    }

    try {
      const resp = await apiFetch(`/fishing-session/${fishingSessionId}/rod/${rodId}/fish`, {
        method: 'DELETE',
        headers: {
          'sessionId': sessionId
        }
      });

      if (resp.ok) {
        const data = await resp.json();
        counterValue.textContent = data.fishCount;
        showToast('Poisson retiré', 'info');
      }
    } catch (err) {
      console.error('Erreur retrait poisson:', err);
      showToast('Erreur lors du retrait du poisson', 'danger');
    }
  });

  // Bouton fermer (supprimer la canne)
  closeBtn.addEventListener('click', async () => {
    if (!confirm(`Supprimer la canne "${rodName}" ?`)) return;

    try {
      const resp = await apiFetch(`/fishing-session/${fishingSessionId}/rod/${rodId}`, {
        method: 'DELETE',
        headers: {
          'sessionId': sessionId
        }
      });

      if (resp.ok) {
        stopTimer();
        card.remove();
        showToast('Canne supprimée', 'success');
      }
    } catch (err) {
      console.error('Erreur suppression canne:', err);
      showToast('Erreur lors de la suppression', 'danger');
    }
  });

  document.querySelector('.session-body').appendChild(card);
}
