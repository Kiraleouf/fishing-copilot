export async function createRodCard(name = "Canne 1") {
  const response = await fetch('/templates/rod-card.html');
  const html = await response.text();

  const wrapper = document.createElement('div');
  wrapper.innerHTML = html;
  const card = wrapper.firstElementChild;

  card.querySelector('.rod-title').textContent = name;

  document.querySelector('.session-body').appendChild(card);
}

