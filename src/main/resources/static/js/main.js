// src/main/resources/static/js/main.js
document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM loaded');

    // 페이지 로드 시 사용자 인증 상태 확인
    const accessToken = localStorage.getItem('accessToken');
    const currentPath = window.location.pathname;

    console.log('Access token exists:', !!accessToken);

    // 로그인 상태에 따라 UI 업데이트
    setTimeout(() => {
        // 약간의 지연을 주어 DOM이 완전히 로드된 후 실행
        updateUIBasedOnLoginStatus();
    }, 100);

    // 토큰이 없고, 보호된 페이지일 경우 로그인 페이지로 리다이렉트
    if (!accessToken &&
        currentPath !== '/' &&
        currentPath !== '/login' &&
        currentPath !== '/signup' &&
        !currentPath.startsWith('/api/')) {
        window.location.href = '/login';
    }

    // 로그아웃 버튼 이벤트 리스너
    const logoutBtn = document.getElementById('logout-btn');
    if (logoutBtn) {
        console.log('Logout button found');
        logoutBtn.addEventListener('click', function(e) {
            e.preventDefault();
            logout();
        });
    } else {
        console.log('Logout button not found');
    }

    // 로그인 폼 이벤트 리스너
    const loginForm = document.getElementById('login-form');
    if (loginForm) {
        console.log('Login form found');
        loginForm.addEventListener('submit', function(e) {
            e.preventDefault();

            if (!validateForm(loginForm)) return;

            const id = document.getElementById('id').value;
            const password = document.getElementById('password').value;
            const loginError = document.getElementById('login-error');

            fetch('/api/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ id, password })
            })
                .then(response => response.json())
                .then(data => {
                    if (data.status === 'success' && data.data) {
                        console.log('Login successful');
                        // 토큰 저장
                        localStorage.setItem('accessToken', data.data.accessToken);
                        localStorage.setItem('refreshToken', data.data.refreshToken);

                        // 홈페이지로 이동
                        window.location.href = '/';
                    } else {
                        console.log('Login failed:', data.message);
                        // 로그인 실패
                        if (loginError) {
                            showError(loginError, data.message || 'Login failed. Please try again.');
                        }
                    }
                })
                .catch(error => {
                    console.error('Login error:', error);
                    if (loginError) {
                        showError(loginError, 'An error occurred. Please try again.');
                    }
                });
        });
    } else {
        console.log('Login form not found');
    }

    // 회원가입 폼 이벤트 리스너
    const signupForm = document.getElementById('signup-form');
    if (signupForm) {
        // 기존 이벤트 리스너 제거를 위한 복제본 생성
        const newSignupForm = signupForm.cloneNode(true);
        signupForm.parentNode.replaceChild(newSignupForm, signupForm);

        console.log('Signup form found, registering event listener');
        newSignupForm.addEventListener('submit', function(e) {
            e.preventDefault();

            if (!validateForm(newSignupForm)) return;

            const signupError = document.getElementById('signup-error');

            const userData = {
                id: document.getElementById('id').value,
                password: document.getElementById('password').value,
                name: document.getElementById('name').value,
                nickname: document.getElementById('nickname').value,
                birthday: document.getElementById('birthday').value,
                phone: document.getElementById('phone').value,
                address: document.getElementById('address').value,
                profileImage: document.getElementById('profileImage')?.value || null
            };

            // 중복 제출 방지를 위해 버튼 비활성화
            const submitButton = newSignupForm.querySelector('button[type="submit"]');
            if (submitButton) {
                submitButton.disabled = true;
                submitButton.innerHTML = 'Processing...';
            }

            fetch('/api/auth/signup', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(userData)
            })
                .then(response => response.json())
                .then(data => {
                    if (data.status === 'success') {
                        console.log('Signup successful');
                        // 회원가입 성공
                        alert('Registration successful! Please login with your credentials.');
                        window.location.href = '/login';
                    } else {
                        console.log('Signup failed:', data.message);
                        // 회원가입 실패
                        if (signupError) {
                            showError(signupError, data.message || 'Registration failed. Please try again.');
                        }
                        // 버튼 다시 활성화
                        if (submitButton) {
                            submitButton.disabled = false;
                            submitButton.innerHTML = 'Sign Up';
                        }
                    }
                })
                .catch(error => {
                    console.error('Signup error:', error);
                    if (signupError) {
                        showError(signupError, 'An error occurred. Please try again.');
                    }
                    // 버튼 다시 활성화
                    if (submitButton) {
                        submitButton.disabled = false;
                        submitButton.innerHTML = 'Sign Up';
                    }
                });
        });
    } else {
        console.log('Signup form not found');
    }

    // mypage-item 클래스를 가진 요소의 클릭 이벤트 리스너 추가
    const mypageLink = document.querySelector('.mypage-item a');
    if (mypageLink) {
        mypageLink.addEventListener('click', function(e) {
            e.preventDefault();
            const token = localStorage.getItem('accessToken');
            if (!token) {
                window.location.href = '/login';
                return;
            }

            // 페이지 이동 전에 토큰 검증
            fetch('/api/auth/validate', {
                method: 'GET',
                headers: {
                    'Authorization': `Bearer ${token}`
                }
            })
                .then(response => {
                    if (response.status === 200) {
                        window.location.href = '/mypage';
                    } else {
                        // 토큰 검증 실패 시 로그인 페이지로 이동
                        localStorage.removeItem('accessToken');
                        localStorage.removeItem('refreshToken');
                        window.location.href = '/login';
                    }
                })
                .catch(error => {
                    console.error('Error validating token:', error);
                    localStorage.removeItem('accessToken');
                    localStorage.removeItem('refreshToken');
                    window.location.href = '/login';
                });
        });
    }

    // 토큰 만료 여부 확인 및 갱신 (매 5분마다)
    if (accessToken) {
        setInterval(checkTokenExpiration, 5 * 60 * 1000);
    }

    // API 요청에 Authorization 헤더 자동 추가
    const originalFetch = window.fetch;
    window.fetch = function() {
        const args = Array.from(arguments);
        const url = args[0];
        const options = args[1] || {};

        // API 요청이고 인증이 필요한 경우에만 헤더 추가
        if (typeof url === 'string' && url.startsWith('/api/') && url !== '/api/auth/login' && url !== '/api/auth/signup') {
            options.headers = options.headers || {};

            const currentToken = localStorage.getItem('accessToken');
            if (currentToken && !options.headers['Authorization']) {
                options.headers['Authorization'] = `Bearer ${currentToken}`;
            }

            args[1] = options;
        }

        return originalFetch.apply(window, args);
    };

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
});

// 로그인 상태에 따라 UI 업데이트
function updateUIBasedOnLoginStatus() {
    const accessToken = localStorage.getItem('accessToken');

    console.log('Updating UI based on login status. Access token exists:', !!accessToken);
    console.log('Login items found:', document.querySelectorAll('.login-item').length);
    console.log('Signup items found:', document.querySelectorAll('.signup-item').length);
    console.log('Mypage items found:', document.querySelectorAll('.mypage-item').length);
    console.log('Logout items found:', document.querySelectorAll('.logout-item').length);

    if (accessToken) {
        // 로그인 상태
        updateUIForLoggedInUser();

        // 토큰 유효성 검증 (선택적, 페이지 로드 시 토큰 유효성 확인)
        validateToken(accessToken)
            .then(isValid => {
                console.log('Token validation result:', isValid);
                if (!isValid) {
                    logout();
                }
            })
            .catch(error => {
                console.error('Token validation error:', error);
            });
    } else {
        // 로그아웃 상태
        updateUIForLoggedOutUser();
    }
}

// 토큰 유효성 검사
function validateToken(token) {
    return fetch('/api/auth/validate', {
        method: 'GET',
        headers: {
            'Authorization': `Bearer ${token}`
        }
    })
        .then(response => {
            return response.status === 200;
        })
        .catch(error => {
            console.error('Error validating token:', error);
            return false;
        });
}

// 로그인 상태 UI 업데이트
function updateUIForLoggedInUser() {
    console.log('Updating UI for logged in user');
    // 로그인/회원가입 버튼 숨기기
    document.querySelectorAll('.login-item, .signup-item').forEach(item => {
        console.log('Hiding item:', item);
        item.classList.add('d-none');
    });

    // 마이페이지/로그아웃 버튼 표시
    document.querySelectorAll('.mypage-item, .logout-item').forEach(item => {
        console.log('Showing item:', item);
        item.classList.remove('d-none');
    });
}

// 로그아웃 상태 UI 업데이트
function updateUIForLoggedOutUser() {
    console.log('Updating UI for logged out user');
    // 로그인/회원가입 버튼 표시
    document.querySelectorAll('.login-item, .signup-item').forEach(item => {
        console.log('Showing item:', item);
        item.classList.remove('d-none');
    });

    // 마이페이지/로그아웃 버튼 숨기기
    document.querySelectorAll('.mypage-item, .logout-item').forEach(item => {
        console.log('Hiding item:', item);
        item.classList.add('d-none');
    });
}

// 로그아웃 함수
function logout() {
    console.log('Logging out');
    // 토큰 제거
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');

    // UI 업데이트
    updateUIForLoggedOutUser();

    // 현재 페이지가 마이페이지면 홈으로 이동
    if (window.location.pathname === '/mypage') {
        window.location.href = '/';
    } else {
        // 다른 페이지에서는 로그인 페이지로 이동
        window.location.href = '/login';
    }
}

// 토큰 만료 여부 확인 및 갱신
function checkTokenExpiration() {
    const accessToken = localStorage.getItem('accessToken');
    const refreshToken = localStorage.getItem('refreshToken');

    if (!accessToken || !refreshToken) {
        return;
    }

    // 토큰 갱신 요청
    fetch('/api/auth/refresh', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            refreshToken: refreshToken
        })
    })
        .then(response => response.json())
        .then(data => {
            if (data.status === 'success' && data.data) {
                console.log('Token refreshed successfully');
                // 새 토큰 저장
                localStorage.setItem('accessToken', data.data.accessToken);
                localStorage.setItem('refreshToken', data.data.refreshToken);
            } else {
                console.log('Token refresh failed');
                // 갱신 실패 시 로그아웃
                logout();
            }
        })
        .catch(error => {
            console.error('Error refreshing token:', error);
            logout();
        });
}

// 폼 유효성 검사 함수
function validateForm(form) {
    const inputs = form.querySelectorAll('input[required]');
    let isValid = true;

    inputs.forEach(input => {
        if (!input.value.trim()) {
            input.classList.add('is-invalid');
            isValid = false;
        } else {
            input.classList.remove('is-invalid');
        }
    });

    return isValid;
}

// 날짜 포맷팅 함수
function formatDate(dateString) {
    const date = new Date(dateString);
    return date.toLocaleDateString();
}

// 에러 메시지 표시 함수
function showError(element, message) {
    element.textContent = message;
    element.classList.remove('d-none');
}

// 에러 메시지 숨기기 함수
function hideError(element) {
    element.classList.add('d-none');
}