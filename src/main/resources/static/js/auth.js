// src/main/resources/static/js/auth.js

const Auth = {
  /**
   * 페이지 이동: URL에 accessToken을 추가하여 이동
   * @param {string} url - 이동할 URL
   */
  navigate: function(url) {
    if (url === '/logout') {
      this.logout();
      return;
    }

    const accessToken = localStorage.getItem('accessToken');

    // admin 페이지 접근 시 토큰 체크
    if (url.startsWith('/admin/')) {
      if (!accessToken) {
        window.location.href = '/error/403';
        return;
      }

      // URL accessToken 추가
      const separator = url.includes('?') ? '&' : '?';
      const finalUrl = `${url}${separator}accessToken=${accessToken}`;
      window.location.href = finalUrl;
      return;
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
    const currentPath = window.location.pathname;

    // 예외 URL 목록
    const publicUrls = ['/login', '/error/403', '/error/404', '/error/500'];
    if (publicUrls.includes(currentPath)) {
      return;
    }

    // admin 페이지 접근 시 토큰 체크
    if (currentPath.startsWith('/admin/')) {
      const accessToken = localStorage.getItem('accessToken');
      if (!accessToken) {
        window.location.href = '/error/403';
      }
    }
  }
};

// 페이지 로드 시 accessToken 확인
document.addEventListener('DOMContentLoaded', Auth.checkAccessToken);