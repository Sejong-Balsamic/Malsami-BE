// src/main/resources/static/js/auth.js

const Auth = {
  /**
   * 페이지 이동: URL에 accessToken을 추가하여 이동
   * @param {string} url - 이동할 URL
   */
  navigate: function(url) {
    // 로그아웃 처리
    if (url === '/logout') {
      this.logout();
      return;
    }

    const accessToken = localStorage.getItem('accessToken');
    if (!accessToken) {
      window.location.href = '/login';
      return;
    }

    // admin 페이지로 이동할 때 accessToken 추가
    if (url.startsWith('/admin/')) {
      url = url + (url.includes('?') ? '&' : '?') + `accessToken=${accessToken}`;
    }
    window.location.href = url;
  },

  /**
   * 로그아웃: localStorage 클리어 후 로그인 페이지로 이동
   */
  logout: function() {
    localStorage.clear();
    window.location.href = '/login';
  },

  /**
   * accessToken 확인
   */
  checkAccessToken: function() {
    // 로그인 페이지에서는 토큰 삭제
    if (window.location.pathname === '/login') {
      localStorage.clear();
      return;
    }

    const accessToken = localStorage.getItem('accessToken');
    if (!accessToken) {
      window.location.href = '/login';
    }
  }
};

// 페이지 로드 시 accessToken 확인
document.addEventListener('DOMContentLoaded', Auth.checkAccessToken);