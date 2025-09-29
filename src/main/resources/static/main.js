document.addEventListener("DOMContentLoaded", async () => {
  const sessionId = localStorage.getItem("sessionId");
  const dingSound = new Audio("sound/ding.mp3");
  function unlockSound() {
    dingSound
      .play()
      .then(() => {
        dingSound.pause();
        dingSound.currentTime = 0;
      })
      .catch(() => {});
  }
  document.addEventListener("click", unlockSound, { once: true });
  document.addEventListener("touchstart", unlockSound, { once: true });

  async function validateSession() {
    if (!sessionId) return false;
    const resp = await apiFetch("/session/check", { headers: { sessionId } });
    if (resp.ok) {
      const username = await resp.text();
      localStorage.setItem("login", username);
      return true;
    }
    localStorage.removeItem("sessionId");
    localStorage.removeItem("login");
    localStorage.removeItem("currentFishingSessionId");
    return false;
  }

  async function getCurrentFishingSession() {
    const resp = await apiFetch("/fishing-session/current", {
      headers: { sessionId },
    });
    if (resp.status === 200) {
      const data = await resp.json();
      localStorage.setItem("currentFishingSessionId", data.id);
      return data;
    }
    localStorage.removeItem("currentFishingSessionId");
    return null;
  }

  const form = document.getElementById("loginForm");
  if (form) {
    if (sessionId && (await validateSession())) {
      const current = await getCurrentFishingSession();
      window.location.href = current ? "session.html" : "home.html";
      return;
    }
    form.addEventListener("submit", async (e) => {
      e.preventDefault();
      const login = document.getElementById("login").value;
      const password = document.getElementById("password").value;
      const resp = await apiFetch("/sign-in", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ login, password }),
      });
      if (resp.ok) {
        const data = await resp.json();
        localStorage.setItem("sessionId", data.sessionId);
        localStorage.setItem("login", login);
        window.location.href = "home.html";
      }
    });
    const reg = document.getElementById("openRegister");
    if (reg) {
      reg.addEventListener("click", () => {
        window.location.href = "register.html";
      });
    }
    return;
  }

  if (!(await validateSession())) {
    window.location.href = "index.html";
    return;
  }

  const usernameDisplay = document.getElementById("usernameDisplay");
  if (usernameDisplay) {
    usernameDisplay.textContent = localStorage.getItem("login") || "";
  }
  const logoutBtn = document.getElementById("logoutBtn");
  if (logoutBtn) {
    logoutBtn.addEventListener("click", async () => {
      await apiFetch("/fisherman/logout", { headers: { sessionId } });
      localStorage.removeItem("sessionId");
      localStorage.removeItem("login");
      localStorage.removeItem("currentFishingSessionId");
      window.location.href = "index.html";
    });
  }

  const startBtn = document.getElementById("startSession");
  if (startBtn) {
    const current = await getCurrentFishingSession();
    if (current) {
      window.location.href = "session.html";
      return;
    }
    startBtn.addEventListener("click", async () => {
      const name = prompt("Nom de la session", "sans nom") || "sans nom";
      const resp = await apiFetch("/fishing-session/create", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          sessionId: sessionId,
        },
        body: JSON.stringify({ name }),
      });
      if (resp.ok) {
        const data = await resp.json();
        localStorage.setItem("currentFishingSessionId", data.id);
        window.location.href = "session.html";
      }
    });
    return;
  }

  const closeBtn = document.getElementById("closeSession");
  if (closeBtn) {
    const current = await getCurrentFishingSession();
    if (!current) {
      window.location.href = "home.html";
      return;
    }

    const rodContainer = document.getElementById("rodContainer");
    const addRodBtn = document.getElementById("addRod");

    const timerDialog = document.getElementById("timerDialog");
    const timerMinutes = document.getElementById("timerMinutes");
    const timerCancel = document.getElementById("timerCancel");
    const timerOk = document.getElementById("timerOk");
    let resolveTimerDialog;
    timerCancel.addEventListener("click", () => {
      timerDialog.classList.add("d-none");
      timerDialog.classList.remove("d-flex");
      if (resolveTimerDialog) resolveTimerDialog(null);
    });
    timerOk.addEventListener("click", () => {
      const minutes = parseInt(timerMinutes.value);
      timerDialog.classList.add("d-none");
      timerDialog.classList.remove("d-flex");
      if (resolveTimerDialog) resolveTimerDialog(minutes);
    });
    function openTimerDialog(initial) {
      if (timerMinutes.options.length === 0) {
        for (let i = 1; i <= 180; i++) {
          const opt = document.createElement("option");
          opt.value = i;
          opt.textContent = i;
          timerMinutes.appendChild(opt);
        }
      }
      timerMinutes.value = initial || 5;
      timerDialog.classList.remove("d-none");
      timerDialog.classList.add("d-flex");
      return new Promise((res) => {
        resolveTimerDialog = res;
      });
    }

    function createRodCard(rod) {
      const card = document.createElement("div");
      card.className = "rod-card m-3";

      // Header avec nom et bouton supprimer
      const header = document.createElement("div");
      header.className = "rod-card-header";

      const nameContainer = document.createElement("div");
      nameContainer.className = "rod-name-container";
      nameContainer.textContent = rod.name;

      const deleteBtn = document.createElement("button");
      deleteBtn.className = "rod-delete-btn";
      deleteBtn.innerHTML = "×";

      header.appendChild(nameContainer);
      header.appendChild(deleteBtn);

      // Body avec counter, timer et play/stop
      const body = document.createElement("div");
      body.className = "rod-card-body";

      // Counter
      const counter = document.createElement("div");
      counter.className = "rod-counter";

      const counterButtons = document.createElement("div");
      counterButtons.className = "counter-buttons";

      const minus = document.createElement("button");
      minus.className = "counter-btn minus-btn";
      minus.textContent = "−";

      const plus = document.createElement("button");
      plus.className = "counter-btn plus-btn";
      plus.textContent = "+";

      counterButtons.appendChild(minus);
      counterButtons.appendChild(plus);

      const count = document.createElement("span");
      count.className = "count-display";
      count.textContent = rod.fishCount;

      counter.appendChild(counterButtons);
      counter.appendChild(count);

      // Timer
      const timer = document.createElement("div");
      timer.className = "rod-timer";
      timer.textContent = "00:00";

      // Play/Stop button
      const playStopBtn = document.createElement("button");
      playStopBtn.className = "play-stop-btn";
      playStopBtn.innerHTML = '<i class="bi bi-play-fill"></i>';

      body.appendChild(counter);
      body.appendChild(timer);
      body.appendChild(playStopBtn);

      card.appendChild(header);
      card.appendChild(body);

      // Event listeners
      deleteBtn.addEventListener("click", async () => {
        if (confirm("Supprimer cette canne ?")) {
          await apiFetch(`/fishing-session/${current.id}/rod/${rod.id}`, {
            method: "DELETE",
            headers: { sessionId },
          });
          card.remove();
        }
      });

      minus.addEventListener("click", async () => {
        if (parseInt(count.textContent) > 0) {
          const resp = await apiFetch(
            `/fishing-session/${current.id}/rod/${rod.id}/fish`,
            {
              method: "DELETE",
              headers: { sessionId },
            }
          );
          if (resp.ok) {
            const data = await resp.json();
            count.textContent = data.fishCount;
          }
        }
      });

      plus.addEventListener("click", async () => {
        const resp = await apiFetch(
          `/fishing-session/${current.id}/rod/${rod.id}/fish`,
          {
            method: "POST",
            headers: { sessionId },
          }
        );
        if (resp.ok) {
          dingSound.currentTime = 0;
          dingSound.play();
          const data = await resp.json();
          count.textContent = data.fishCount;
          if (intervalId) {
            clearInterval(intervalId);
            intervalId = null;
          }
          remaining = duration;
          updateTimerDisplay();
          card.classList.remove("timer-active", "timer-expired");
          playStopBtn.innerHTML = '<i class="bi bi-play-fill"></i>';
        }
      });

      let duration = 0;
      let remaining = 0;
      let intervalId = null;

      function updateTimerDisplay() {
        const m = String(Math.floor(remaining / 60)).padStart(2, "0");
        const s = String(remaining % 60).padStart(2, "0");
        timer.textContent = `${m}:${s}`;
      }

      function startCountdown() {
        remaining = duration;
        updateTimerDisplay();
        card.classList.add("timer-active");
        card.classList.remove("timer-expired");
        playStopBtn.innerHTML = '<i class="bi bi-stop-fill"></i>';
        intervalId = setInterval(() => {
          remaining--;
          updateTimerDisplay();
          if (remaining <= 0) {
            clearInterval(intervalId);
            intervalId = null;
            card.classList.remove("timer-active");
            card.classList.add("timer-expired");
            playStopBtn.innerHTML = '<i class="bi bi-play-fill"></i>';
            dingSound.currentTime = 0;
            dingSound.play();
          }
        }, 1000);
      }

      function stopCountdown() {
        if (intervalId) {
          clearInterval(intervalId);
          intervalId = null;
          card.classList.remove("timer-active");
          playStopBtn.innerHTML = '<i class="bi bi-play-fill"></i>';
        }
      }

      timer.addEventListener("click", async (e) => {
        e.stopPropagation();
        if (intervalId && !confirm("Changer la durée du timer ?")) {
          return;
        }
        const minutes = await openTimerDialog(duration / 60);
        if (minutes) {
          duration = minutes * 60;
          if (intervalId) stopCountdown();
          updateTimerDisplay();
        }
      });

      playStopBtn.addEventListener("click", () => {
        if (duration === 0) {
          // Si pas de durée définie, ouvrir le dialog
          openTimerDialog(5).then((minutes) => {
            if (minutes) {
              duration = minutes * 60;
              startCountdown();
            }
          });
        } else if (intervalId) {
          stopCountdown();
        } else {
          startCountdown();
        }
      });

      rodContainer.insertBefore(card, addRodBtn.parentElement);
    }
    const rodsResp = await apiFetch(`/fishing-session/${current.id}/rods`, {
      headers: { sessionId },
    });
    if (rodsResp.ok) {
      const rods = await rodsResp.json();
      rods.forEach(createRodCard);
    }

    if (addRodBtn) {
      addRodBtn.addEventListener("click", async () => {
        const count = rodContainer.querySelectorAll(".card").length;
        let name =
          prompt("Nom de la canne", `canne #${count + 1}`) ||
          `canne #${count + 1}`;
        name = name.substring(0, 20);
        const resp = await apiFetch(`/fishing-session/${current.id}/rods`, {
          method: "POST",
          headers: { sessionId, "Content-Type": "application/json" },
          body: JSON.stringify({ name }),
        });
        if (resp.ok) {
          const data = await resp.json();
          createRodCard(data);
        }
      });
    }

    closeBtn.addEventListener("click", async () => {
      await apiFetch("/fishing-session/close", {
        method: "POST",
        headers: { sessionId },
      });
      localStorage.removeItem("currentFishingSessionId");
      window.location.href = "home.html";
    });
  }
});
