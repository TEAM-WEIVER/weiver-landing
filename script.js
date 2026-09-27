const optionCards = Array.from(document.querySelectorAll('.demo-card'));
const selectedOption = document.getElementById('selected-option');
const dialog = document.getElementById('reservation-dialog');
const dialogOption = document.getElementById('dialog-option');
const openReservation = document.getElementById('open-reservation');
const closeDialog = document.querySelector('.dialog-close');
const form = document.getElementById('reservation-form');
const successState = document.querySelector('.success-state');
const closeSuccess = document.getElementById('close-success');

function selectOption(card) {
  optionCards.forEach((item) => {
    const selected = item === card;
    item.classList.toggle('is-selected', selected);
    item.setAttribute('aria-checked', String(selected));
  });
  selectedOption.textContent = card.dataset.option;
}

optionCards.forEach((card) => {
  card.addEventListener('click', () => selectOption(card));
});

openReservation.addEventListener('click', () => {
  dialogOption.value = selectedOption.textContent;
  form.hidden = false;
  successState.hidden = true;
  dialog.showModal();
});

closeDialog.addEventListener('click', () => dialog.close());
closeSuccess.addEventListener('click', () => dialog.close());

dialog.addEventListener('click', (event) => {
  if (event.target === dialog) dialog.close();
});

form.addEventListener('submit', (event) => {
  event.preventDefault();
  if (!form.reportValidity()) return;
  form.hidden = true;
  successState.hidden = false;
});
