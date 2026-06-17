// admin-common.js — 관리자 공통 유틸 (jQuery 비의존)

function showToast(message, type = 'info', duration = 3000) {
  const container = document.getElementById('toast-container');
  if (!container) { console.warn('toast-container 없음'); return; }
  const colorMap = { info: 'alert-info', success: 'alert-success', error: 'alert-error', warning: 'alert-warning' };
  const el = document.createElement('div');
  el.className = `alert ${colorMap[type] || 'alert-info'} shadow`;
  el.textContent = message;
  container.appendChild(el);
  setTimeout(() => { el.remove(); }, duration);
}

// accessToken을 헤더 + 쿼리로 첨부한 POST (기존 페이지 호출 규약과 동일)
function adminFetch(url, formData) {
  const token = localStorage.getItem('accessToken');
  const sep = url.includes('?') ? '&' : '?';
  return fetch(url + sep + 'accessToken=' + token, {
    method: 'POST',
    headers: { 'Authorization': 'Bearer ' + token },
    body: formData
  }).then(res => {
    if (res.status === 401 || res.status === 403) {
      showToast('인증이 만료되었습니다. 다시 로그인해주세요.', 'error');
      setTimeout(() => Auth.logout(), 1500);
      throw new Error('unauthorized');
    }
    return res.json();
  });
}
