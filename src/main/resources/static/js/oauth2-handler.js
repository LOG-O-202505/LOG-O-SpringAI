// src/main/resources/static/js/oauth2-handler.js
// 이 파일을 프로젝트에 추가

document.addEventListener('DOMContentLoaded', function() {
    const loginWithGoogle = document.getElementById('login-with-google');
    if (loginWithGoogle) {
        loginWithGoogle.addEventListener('click', function() {
            // 현재 페이지 URL을 리다이렉트 URI로 저장 (프론트엔드용)
            const redirectUri = window.location.origin + '/oauth2/redirect';
            const encodedRedirectUri = encodeURIComponent(redirectUri);

            // 기본 인증 엔드포인트 사용
            window.location.href = `/oauth2/authorization/google?redirect_uri=${encodedRedirectUri}`;
        });
    }

    // Naver 로그인 버튼도 비슷하게 수정
    const loginWithNaver = document.getElementById('login-with-naver');
    if (loginWithNaver) {
        loginWithNaver.addEventListener('click', function() {
            const redirectUri = window.location.origin + '/oauth2/redirect';
            const encodedRedirectUri = encodeURIComponent(redirectUri);
            window.location.href = `/oauth2/authorization/naver?redirect_uri=${encodedRedirectUri}`;
        });
    }

    // OAuth2 리다이렉트 처리
    if (window.location.pathname.startsWith('/oauth2/redirect')) {
        // URL에서 토큰 정보 추출
        const urlParams = new URLSearchParams(window.location.search);
        const token = urlParams.get('token');
        const error = urlParams.get('error');

        if (error) {
            console.error('OAuth2 error:', error);
            // 에러 메시지 표시
            showError(error);
        } else if (token) {
            // 토큰 저장
            localStorage.setItem('accessToken', token);
            // 홈페이지로 리다이렉트
            window.location.href = '/';
        }
    }

    // 에러 메시지 표시 함수
    function showError(message) {
        const errorEl = document.createElement('div');
        errorEl.className = 'alert alert-danger';
        errorEl.textContent = message;
        document.body.prepend(errorEl);
    }
});