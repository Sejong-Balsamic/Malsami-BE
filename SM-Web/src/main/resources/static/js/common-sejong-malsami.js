/**
 * showToast(message, duration)
 * @param {string} message   - 표시할 메시지
 * @param {number} duration  - 표시 후 사라질 때까지의 밀리초 (기본 3000)
 */
function showToast(message, duration = 3000) {
  const container = document.getElementById('toast-container');
  if (!container) {
    console.warn('Toast container element not found! (id="toast-container")');
    return;
  }

  // 1) Toast DOM 생성
  const toast = document.createElement('div');
  toast.classList.add('toast'); // 위에서 정의한 .toast CSS 사용
  toast.textContent = message;

  // 2) 컨테이너에 추가
  container.appendChild(toast);

  // 3) 애니메이션 종료 후 제거
  toast.addEventListener('animationend', () => {
    container.removeChild(toast);
  });
}
