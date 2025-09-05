function showToast(message, type = 'info') {
  const alert = document.createElement('div');
  alert.className = `alert alert-${type} position-fixed top-0 end-0 m-3`;
  alert.style.zIndex = 9999;
  alert.textContent = message;
  document.body.appendChild(alert);
  setTimeout(() => alert.remove(), 5000);
}

async function apiFetch(url, options) {
  const resp = await fetch(url, options);
  if (!resp.ok) {
    const status = resp.status;
    const messages = {
      400: 'Requête invalide',
      401: 'Non autorisé',
      403: 'Accès interdit',
      404: 'Ressource introuvable',
      409: 'Conflit',
      500: 'Erreur interne du serveur',
      503: 'Service indisponible'
    };
    const msg = messages[status] || `Erreur ${status}`;
    showToast(msg, status >= 500 ? 'danger' : 'warning');
  }
  return resp;
}

window.showToast = showToast;
window.apiFetch = apiFetch;
