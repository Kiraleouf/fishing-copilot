export async function createRodCard(rodData) {
  const response = await fetch('/templates/rod-card.html?v=' + Date.now());
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
  const minusBtn = card.querySelectorAll('.counter-btn')[0];
  const plusBtn = card.querySelectorAll('.counter-btn')[1];
  const closeBtn = card.querySelector('.rod-close-btn');

  // Éléments du time picker
  const timePickerModal = card.querySelector('.time-picker-modal');
  const timePickerOverlay = card.querySelector('.time-picker-overlay');
  const timePickerWheel = card.querySelector('.time-picker-wheel');
  const timePickerCancel = card.querySelector('.time-picker-cancel');
  const timePickerConfirm = card.querySelector('.time-picker-confirm');

  // Vérification que tous les éléments existent
  if (!timePickerWheel) {
    console.error('timePickerWheel introuvable dans le template !');
    console.log('Contenu de la carte:', card.innerHTML);
    return;
  }

  // État du timer
  let timerSeconds = 0;
  let initialTimerSeconds = 0; // Sauvegarder le temps initial choisi
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
      const itemTop = targetItem.offsetTop;
      const scrollPosition = itemTop - container.clientHeight / 2 + targetItem.clientHeight / 2;
      container.scrollTop = scrollPosition;
    }
  }

  function openTimePicker() {
    console.log('Opening time picker...');
    timePickerModal.classList.add('active');
    // Scroller à la valeur actuelle après un court délai
    const currentMinutes = Math.floor(timerSeconds / 60);
    setTimeout(() => {
      scrollToMinute(currentMinutes);
      updateWheelSelection();
    }, 100);
  }

  function closeTimePicker() {
    console.log('Closing time picker...');
    timePickerModal.classList.remove('active');
  }

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

        // Restaurer le temps initial pour permettre de relancer facilement
        timerSeconds = initialTimerSeconds;
        updateTimerDisplay();
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

  // AJOUTER au DOM à la fin, après que tout soit configuré
  const sessionBody = document.querySelector('.session-body');
  if (sessionBody) {
    sessionBody.appendChild(card);

    // Initialiser le wheel picker APRÈS l'ajout au DOM
    initializeTimePicker();

    // Ouvrir le sélecteur de temps au clic sur le timer (APRÈS l'ajout au DOM)
    timerDisplay.addEventListener('click', (e) => {
      console.log('Timer clicked, isPlaying:', isPlaying, 'isPaused:', isPaused, 'timerInterval:', timerInterval);
      // Ne permettre le changement QUE si le timer n'est pas en cours d'exécution
      if (isPlaying && !isPaused) {
        console.log('Timer is running, cannot change time');
        return;
      }
      e.preventDefault();
      e.stopPropagation();
      openTimePicker();
    });

    // Événements du time picker
    let scrollTimeout;
    let isScrolling = false;

    timePickerWheel.addEventListener('scroll', () => {
      clearTimeout(scrollTimeout);
      isScrolling = true;

      // Mettre à jour visuellement pendant le scroll
      updateWheelSelection();

      // Attendre que l'utilisateur arrête de scroller
      scrollTimeout = setTimeout(() => {
        isScrolling = false;
        const selectedValue = updateWheelSelection();

        // Snap UNIQUEMENT si l'utilisateur a fini de scroller
        const items = timePickerWheel.querySelectorAll('.time-picker-item');
        const selectedItem = Array.from(items).find(item => parseInt(item.dataset.value) === selectedValue);
        if (selectedItem && !isScrolling) {
          const container = timePickerWheel;
          const itemTop = selectedItem.offsetTop;
          const targetScroll = itemTop - container.clientHeight / 2 + selectedItem.clientHeight / 2;
          const currentScroll = container.scrollTop;

          // Snap seulement si la différence est significative (évite la boucle infinie)
          if (Math.abs(targetScroll - currentScroll) > 5) {
            container.scrollTo({
              top: targetScroll,
              behavior: 'smooth'
            });
          }
        }
      }, 150);
    });

    timePickerOverlay.addEventListener('click', (e) => {
      e.preventDefault();
      e.stopPropagation();
      closeTimePicker();
    });

    timePickerCancel.addEventListener('click', (e) => {
      e.preventDefault();
      e.stopPropagation();
      closeTimePicker();
    });

    timePickerConfirm.addEventListener('click', (e) => {
      e.preventDefault();
      e.stopPropagation();
      const selectedMinutes = updateWheelSelection();
      timerSeconds = selectedMinutes * 60;
      initialTimerSeconds = timerSeconds; // Sauvegarder le temps choisi
      updateTimerDisplay();
      setCardState('');
      closeTimePicker();
    });
  } else {
    console.error('Élément .session-body introuvable !');
  }
}
