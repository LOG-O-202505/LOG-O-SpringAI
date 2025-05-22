// src/main/resources/static/js/main.js
document.addEventListener('DOMContentLoaded', function() {
    console.log('DOM loaded');

    // Check authentication status and update UI immediately
    // Do NOT check onboarding status here - only in oauth2-onboarding.js
    checkAuthenticationAndUpdateUI();

    // Get current path for navigation logic
    const currentPath = window.location.pathname;
    console.log('Current path:', currentPath);

    // Set up logout button event listener
    const logoutBtn = document.getElementById('logout-btn');
    if (logoutBtn) {
        console.log('Logout button found');
        logoutBtn.addEventListener('click', function(e) {
            e.preventDefault();
            logout();
        });
    }

    // Set up login form event listener
    const loginForm = document.getElementById('login-form');
    if (loginForm) {
        console.log('Login form found');
        loginForm.addEventListener('submit', function(e) {
            e.preventDefault();
            handleLoginFormSubmit(this);
        });
    }

    // Set up signup form event listener
    const signupForm = document.getElementById('signup-form');
    if (signupForm) {
        console.log('Signup form found');
        signupForm.addEventListener('submit', function(e) {
            e.preventDefault();
            handleSignupFormSubmit(this);
        });
    }

    // Set up mypage link event listener
    const mypageLink = document.querySelector('.mypage-item a');
    if (mypageLink) {
        mypageLink.addEventListener('click', function(e) {
            e.preventDefault();
            handleMypageNavigation();
        });
    }

    // Set up periodic token validation (every 5 minutes)
    const accessToken = getAccessTokenFromCookie();
    if (accessToken) {
        setInterval(checkTokenExpiration, 5 * 60 * 1000);
    }

    // Set up real-time duplicate checking
    setupRealTimeValidation();
});

// Check authentication status and update UI (NO onboarding check)
function checkAuthenticationAndUpdateUI() {
    const accessToken = getAccessTokenFromCookie();
    console.log('Access token exists:', !!accessToken);

    if (accessToken) {
        // Validate token with server
        validateTokenWithServer(accessToken)
            .then(isValid => {
                console.log('Token validation result:', isValid);
                if (isValid) {
                    updateUIForLoggedInUser();
                } else {
                    clearAuthenticationAndUpdateUI();
                }
            })
            .catch(error => {
                console.error('Token validation error:', error);
                clearAuthenticationAndUpdateUI();
            });
    } else {
        updateUIForLoggedOutUser();
    }
}

// Get access token from cookie
function getAccessTokenFromCookie() {
    const cookies = document.cookie.split(';');
    for (let cookie of cookies) {
        const [name, value] = cookie.trim().split('=');
        if (name === 'access_token') {
            return value;
        }
    }
    return null;
}

// Validate token with server
function validateTokenWithServer(token) {
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

// Update UI for logged in user
function updateUIForLoggedInUser() {
    console.log('Updating UI for logged in user');

    // Hide login/signup items
    const loginItems = document.querySelectorAll('.login-item, .signup-item');
    loginItems.forEach(item => {
        console.log('Hiding login/signup item:', item);
        item.classList.add('d-none');
    });

    // Show mypage/logout items
    const userItems = document.querySelectorAll('.mypage-item, .logout-item');
    userItems.forEach(item => {
        console.log('Showing user item:', item);
        item.classList.remove('d-none');
    });
}

// Update UI for logged out user
function updateUIForLoggedOutUser() {
    console.log('Updating UI for logged out user');

    // Show login/signup items
    const loginItems = document.querySelectorAll('.login-item, .signup-item');
    loginItems.forEach(item => {
        console.log('Showing login/signup item:', item);
        item.classList.remove('d-none');
    });

    // Hide mypage/logout items
    const userItems = document.querySelectorAll('.mypage-item, .logout-item');
    userItems.forEach(item => {
        console.log('Hiding user item:', item);
        item.classList.add('d-none');
    });
}

// Clear authentication and update UI
function clearAuthenticationAndUpdateUI() {
    // Clear cookies by setting them to expire
    document.cookie = 'access_token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
    document.cookie = 'refresh_token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
    // Also clear any onboarding cookies
    document.cookie = 'is_new_user=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
    document.cookie = 'user_id=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';

    // Update UI
    updateUIForLoggedOutUser();
}

// Handle login form submission
function handleLoginFormSubmit(form) {
    if (!validateForm(form)) return;

    const id = document.getElementById('id').value;
    const password = document.getElementById('password').value;
    const loginError = document.getElementById('login-error');
    const submitButton = form.querySelector('button[type="submit"]');

    // Show loading state
    submitButton.disabled = true;
    submitButton.innerHTML = '<i class="fas fa-spinner fa-spin mr-2"></i>Signing In...';
    loginError.classList.add('d-none');

    fetch('/api/auth/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ id, password })
    })
        .then(response => response.json())
        .then(data => {
            if (data.status === 'success') {
                console.log('Login successful');
                submitButton.innerHTML = '<i class="fas fa-check mr-2"></i>Success!';

                // Update UI immediately
                updateUIForLoggedInUser();

                // Redirect after short delay
                setTimeout(() => {
                    window.location.href = '/';
                }, 1000);
            } else {
                console.log('Login failed:', data.message);
                showError(loginError, data.message || 'Login failed. Please try again.');
                submitButton.disabled = false;
                submitButton.innerHTML = '<i class="fas fa-sign-in-alt mr-2"></i>Sign In';
            }
        })
        .catch(error => {
            console.error('Login error:', error);
            showError(loginError, 'An error occurred. Please try again.');
            submitButton.disabled = false;
            submitButton.innerHTML = '<i class="fas fa-sign-in-alt mr-2"></i>Sign In';
        });
}

// Handle signup form submission
function handleSignupFormSubmit(form) {
    if (!validateSignupForm(form)) return;

    const signupError = document.getElementById('signup-error');
    const submitButton = form.querySelector('button[type="submit"]');

    const userData = {
        id: document.getElementById('id').value,
        password: document.getElementById('password').value,
        name: document.getElementById('name').value,
        nickname: document.getElementById('nickname').value,
        email: document.getElementById('email').value,
        gender: document.getElementById('gender').value,
        birthday: document.getElementById('birthday').value,
        profileImage: document.getElementById('profileImage')?.value || null
    };

    // Show loading state
    submitButton.disabled = true;
    submitButton.innerHTML = '<i class="fas fa-spinner fa-spin mr-2"></i>Creating Account...';

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
                alert('Registration successful! Please login with your credentials.');
                window.location.href = '/login';
            } else {
                console.log('Signup failed:', data.message);
                showError(signupError, data.message || 'Registration failed. Please try again.');
            }
        })
        .catch(error => {
            console.error('Signup error:', error);
            showError(signupError, 'An error occurred. Please try again.');
        })
        .finally(() => {
            submitButton.disabled = false;
            submitButton.innerHTML = '<i class="fas fa-user-plus mr-2"></i>Sign Up';
        });
}

// Handle mypage navigation
function handleMypageNavigation() {
    const token = getAccessTokenFromCookie();
    if (!token) {
        window.location.href = '/login';
        return;
    }

    // Validate token before navigation
    validateTokenWithServer(token)
        .then(isValid => {
            if (isValid) {
                window.location.href = '/mypage';
            } else {
                clearAuthenticationAndUpdateUI();
                window.location.href = '/login';
            }
        })
        .catch(error => {
            console.error('Error validating token:', error);
            clearAuthenticationAndUpdateUI();
            window.location.href = '/login';
        });
}

// Logout function
function logout() {
    console.log('Logging out');

    // Call logout API
    fetch('/api/auth/logout', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
        .then(response => response.json())
        .then(data => {
            console.log('Logout response:', data);
        })
        .catch(error => {
            console.error('Logout error:', error);
        })
        .finally(() => {
            // Always clear authentication and update UI
            clearAuthenticationAndUpdateUI();

            // Redirect based on current page
            if (window.location.pathname === '/mypage') {
                window.location.href = '/';
            } else {
                window.location.href = '/login';
            }
        });
}

// Check token expiration and refresh if needed
function checkTokenExpiration() {
    const accessToken = getAccessTokenFromCookie();
    const refreshToken = getRefreshTokenFromCookie();

    if (!accessToken || !refreshToken) {
        return;
    }

    // Try to refresh token
    fetch('/api/auth/refresh', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }
    })
        .then(response => response.json())
        .then(data => {
            if (data.status === 'success') {
                console.log('Token refreshed successfully');
            } else {
                console.log('Token refresh failed');
                logout();
            }
        })
        .catch(error => {
            console.error('Error refreshing token:', error);
            logout();
        });
}

// Get refresh token from cookie
function getRefreshTokenFromCookie() {
    const cookies = document.cookie.split(';');
    for (let cookie of cookies) {
        const [name, value] = cookie.trim().split('=');
        if (name === 'refresh_token') {
            return value;
        }
    }
    return null;
}

// Form validation functions
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

function validateSignupForm(form) {
    const inputs = form.querySelectorAll('input[required], select[required]');
    let isValid = true;

    inputs.forEach(input => {
        if (!input.value.trim()) {
            input.classList.add('is-invalid');
            isValid = false;
        } else {
            input.classList.remove('is-invalid');
        }
    });

    // Email format validation
    const emailInput = document.getElementById('email');
    if (emailInput && !isValidEmail(emailInput.value)) {
        emailInput.classList.add('is-invalid');
        isValid = false;
    }

    // Check for any invalid inputs
    const invalidInputs = form.querySelectorAll('.is-invalid');
    if (invalidInputs.length > 0) {
        isValid = false;
    }

    return isValid;
}

// Utility functions
function isValidEmail(email) {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
}

function showError(element, message) {
    element.textContent = message;
    element.classList.remove('d-none');
}

function hideError(element) {
    element.classList.add('d-none');
}

// Real-time validation setup
function setupRealTimeValidation() {
    // Email duplicate check
    const emailInput = document.getElementById('email');
    if (emailInput) {
        let emailTimeout;
        emailInput.addEventListener('input', function() {
            clearTimeout(emailTimeout);
            const email = this.value.trim();

            if (email && isValidEmail(email)) {
                emailTimeout = setTimeout(() => {
                    checkEmailDuplicate(email);
                }, 500);
            }
        });
    }

    // Nickname duplicate check
    const nicknameInput = document.getElementById('nickname');
    if (nicknameInput) {
        let nicknameTimeout;
        nicknameInput.addEventListener('input', function() {
            clearTimeout(nicknameTimeout);
            const nickname = this.value.trim();

            if (nickname.length >= 2) {
                nicknameTimeout = setTimeout(() => {
                    checkNicknameDuplicate(nickname);
                }, 500);
            }
        });
    }
}

function checkEmailDuplicate(email) {
    fetch(`/api/users/exists/email/${encodeURIComponent(email)}`)
        .then(response => response.json())
        .then(data => {
            const emailInput = document.getElementById('email');
            const emailFeedback = document.getElementById('email-feedback') || createFeedbackElement('email');

            if (data.exists) {
                emailInput.classList.add('is-invalid');
                emailInput.classList.remove('is-valid');
                emailFeedback.textContent = 'This email is already in use.';
                emailFeedback.className = 'form-text text-danger';
            } else {
                emailInput.classList.remove('is-invalid');
                emailInput.classList.add('is-valid');
                emailFeedback.textContent = 'This email is available.';
                emailFeedback.className = 'form-text text-success';
            }
        })
        .catch(error => {
            console.error('Email check error:', error);
        });
}

function checkNicknameDuplicate(nickname) {
    fetch(`/api/users/exists/nickname/${encodeURIComponent(nickname)}`)
        .then(response => response.json())
        .then(data => {
            const nicknameInput = document.getElementById('nickname');
            const nicknameFeedback = document.getElementById('nickname-feedback');

            if (data.exists) {
                nicknameInput.classList.add('is-invalid');
                nicknameInput.classList.remove('is-valid');
                nicknameFeedback.textContent = 'This nickname is already in use.';
                nicknameFeedback.className = 'form-text text-danger';
            } else {
                nicknameInput.classList.remove('is-invalid');
                nicknameInput.classList.add('is-valid');
                nicknameFeedback.textContent = 'This nickname is available.';
                nicknameFeedback.className = 'form-text text-success';
            }
        })
        .catch(error => {
            console.error('Nickname check error:', error);
        });
}

function createFeedbackElement(fieldName) {
    const feedback = document.createElement('small');
    feedback.id = fieldName + '-feedback';
    feedback.className = 'form-text';

    const input = document.getElementById(fieldName);
    input.parentNode.appendChild(feedback);

    return feedback;
}

// Date formatting utility
function formatDate(dateString) {
    if (!dateString) return '';
    const date = new Date(dateString);
    return date.toLocaleDateString();
}