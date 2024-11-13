// src/main/resources/static/js/auth.js

const Auth = {
  /**
   * 페이지 이동: URL에 accessToken을 추가하여 이동
   * @param {string} url - 이동할 URL
   */
  navigate: function(url) {
    const accessToken = localStorage.getItem('accessToken');
    if (!accessToken) {
      window.location.href = '/login'; // accessToken 없으면 로그인 페이지로 이동
      return;
    }

    // admin 페이지로 이동할 때 accessToken 추가
    if (url.startsWith('/admin/')) {
      url = url + (url.includes('?') ? '&' : '?') + `accessToken=${accessToken}`;
    }
    window.location.href = url;
  },

  /**
   * 로그아웃: accessToken 제거 후 로그인 페이지로 이동
   */
  logout: function() {
    localStorage.removeItem('accessToken'); // accessToken 제거
    window.location.href = '/login'; // 로그인 페이지로 이동
  },

  /**
   * accessToken 확인: 없으면 로그인 페이지로 리다이렉트
   */
  checkAccessToken: function() {
    const accessToken = localStorage.getItem('accessToken');
    if (!accessToken) {
      window.location.href = '/login'; // accessToken 없으면 로그인 페이지로 이동
    }
  }
};

// 페이지 로드 시 accessToken 확인
document.addEventListener('DOMContentLoaded', Auth.checkAccessToken);
