document.addEventListener('DOMContentLoaded', async () => {
    const sessionNameElement = document.getElementById('sessionName');
    const sessionDateElement = document.getElementById('sessionDate');
    const rodsListElement = document.getElementById('rodsList');
    const photosGridElement = document.getElementById('photosGrid');
    const backToHomeButton = document.getElementById('backToHomeButton');

    const sessionId = localStorage.getItem('currentFishingSessionId');

    if (!sessionId) {
        console.error('Aucune session en cours trouvée. Redirection vers la page d\'accueil.');
        window.location.href = 'home.html';
        return;
    }

    try {
        const response = await fetch(`/fishing-session/${sessionId}`, {
            headers: { sessionId: localStorage.getItem('sessionId') }
        });

        if (!response.ok) {
            throw new Error('Erreur lors de la récupération des données de la session');
        }

        const sessionData = await response.json();

        // Mettre à jour le nom et la date de la session
        sessionNameElement.textContent = `Session : ${sessionData.name}`;
        sessionDateElement.textContent = new Date(sessionData.date).toLocaleDateString('fr-FR', {
            weekday: 'long', year: 'numeric', month: 'long', day: 'numeric'
        });

        // Mettre à jour la liste des cannes
        rodsListElement.innerHTML = ''; // Vider la liste existante
        sessionData.rods.forEach(rod => {
            const rodElement = document.createElement('div');
            rodElement.className = 'session-item';
            rodElement.style.display = 'flex';
            rodElement.style.justifyContent = 'space-between';

            rodElement.innerHTML = `
                <span>${rod.name}</span>
                <span><strong>${rod.fishCount} poissons</strong></span>
            `;

            rodsListElement.appendChild(rodElement);
        });

        console.log('Liste des photos reçues :', sessionData.pictures); // Log pour déboguer les chemins des photos

        // Afficher les photos
        photosGridElement.innerHTML = ''; // Vider la grille existante
        sessionData.pictures.forEach(picture => {
            if (!picture.imgPath) {
                console.error('Chemin de photo manquant :', picture);
                return;
            }

            const imgElement = document.createElement('img');
            imgElement.src = `/photos/${picture.imgPath}`;
            imgElement.alt = 'Photo de session';
            imgElement.style.width = '100%';
            imgElement.style.height = 'auto';
            imgElement.style.cursor = 'pointer';

            // Ajouter un événement pour afficher en plein écran
            imgElement.addEventListener('click', () => {
                const fullScreenDiv = document.createElement('div');
                fullScreenDiv.style.position = 'fixed';
                fullScreenDiv.style.top = '0';
                fullScreenDiv.style.left = '0';
                fullScreenDiv.style.width = '100%';
                fullScreenDiv.style.height = '100%';
                fullScreenDiv.style.backgroundColor = 'rgba(0, 0, 0, 0.8)';
                fullScreenDiv.style.display = 'flex';
                fullScreenDiv.style.justifyContent = 'center';
                fullScreenDiv.style.alignItems = 'center';
                fullScreenDiv.style.zIndex = '1000';

                const fullScreenImg = document.createElement('img');
                fullScreenImg.src = `/photos/${picture.imgPath}`;
                fullScreenImg.alt = 'Photo de session';
                fullScreenImg.style.maxWidth = '90%';
                fullScreenImg.style.maxHeight = '90%';

                fullScreenDiv.appendChild(fullScreenImg);

                // Fermer la vue plein écran au clic
                fullScreenDiv.addEventListener('click', () => {
                    document.body.removeChild(fullScreenDiv);
                });

                document.body.appendChild(fullScreenDiv);
            });

            photosGridElement.appendChild(imgElement);
        });
    } catch (error) {
        console.error('Erreur lors du chargement des données de la session :', error);
    }

    if (backToHomeButton) {
        backToHomeButton.addEventListener('click', () => {
            window.location.href = 'home.html';
        });
    }
});
