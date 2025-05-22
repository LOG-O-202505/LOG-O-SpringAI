// src/main/resources/static/js/oauth2-onboarding.js
document.addEventListener('DOMContentLoaded', function() {
    // Only check onboarding status if there are specific indicators
    checkOnboardingStatusConditionally();
});

// Check onboarding status only under specific conditions
function checkOnboardingStatusConditionally() {
    // Check URL parameters first - this indicates OAuth redirect
    const urlParams = new URLSearchParams(window.location.search);
    const hasOnboardingParam = urlParams.get('onboarding') === 'true';

    // Check if user just completed OAuth login
    const hasAuthenticationCookies = hasValidAuthenticationCookies();

    // Only proceed if there are clear indicators of OAuth login
    if (hasOnboardingParam || (hasAuthenticationCookies && hasNewUserCookie())) {
        console.log('Checking onboarding status due to OAuth login indicators');
        checkOnboardingStatus();
    } else {
        console.log('No OAuth login indicators found, skipping onboarding check');
    }
}

// Check if user has valid authentication cookies (indicating recent login)
function hasValidAuthenticationCookies() {
    const accessToken = getCookie('access_token');
    const refreshToken = getCookie('refresh_token');
    return !!(accessToken && refreshToken);
}

// Check if user has new user cookie
function hasNewUserCookie() {
    const isNewUser = getCookie('is_new_user');
    return isNewUser === 'true';
}

// Check onboarding status from server
function checkOnboardingStatus() {
    fetch('/api/oauth2/onboarding/status')
        .then(response => response.json())
        .then(data => {
            console.log('Onboarding status response:', data);

            // Only show modal if user is authenticated AND needs additional info
            if (data.status === 'success' &&
                data.data.isNewUser &&
                data.data.needsAdditionalInfo &&
                hasValidAuthenticationCookies()) {

                console.log('User needs additional info:', data.data);
                showOnboardingModal(data.data);
            } else {
                console.log('User does not need onboarding or is not authenticated');
                // Clear any stale onboarding cookies
                clearOnboardingCookies();
            }
        })
        .catch(error => {
            console.error('Error checking onboarding status:', error);
            // Clear cookies on error to prevent stuck state
            clearOnboardingCookies();
        });
}

// Show onboarding modal only for authenticated users
function showOnboardingModal(userData = null) {
    // Double-check authentication before showing modal
    if (!hasValidAuthenticationCookies()) {
        console.log('User not authenticated, not showing onboarding modal');
        return;
    }

    console.log('Showing onboarding modal for authenticated user');

    // Fill form with user data if available
    if (userData && userData.userId) {
        document.getElementById('onboarding-user-id').value = userData.userId;
    }

    // Add complete button event listener (if not already added)
    const completeBtn = document.getElementById('complete-onboarding-btn');
    if (completeBtn && !completeBtn.hasAttribute('data-listener-added')) {
        completeBtn.addEventListener('click', completeOnboarding);
        completeBtn.setAttribute('data-listener-added', 'true');
    }

    // Show modal with backdrop that can't be dismissed
    $('#onboardingModal').modal({
        backdrop: 'static',
        keyboard: false
    });
}

// Complete onboarding process
function completeOnboarding() {
    const form = document.getElementById('onboarding-form');
    const formData = new FormData(form);
    const completionData = {};

    // Convert FormData to object
    for (let [key, value] of formData.entries()) {
        if (value.trim() !== '') {
            if (key === 'userId') {
                completionData[key] = parseInt(value);
            } else {
                completionData[key] = value;
            }
        }
    }

    // Validate required fields
    if (!completionData.userId || !completionData.gender || !completionData.birthday) {
        showOnboardingError('Gender and birthday are required fields.');
        return;
    }

    const completeBtn = document.getElementById('complete-onboarding-btn');
    completeBtn.disabled = true;
    completeBtn.innerHTML = '<i class="fas fa-spinner fa-spin mr-2"></i>Processing...';

    // Send data to server
    fetch('/api/oauth2/complete', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(completionData)
    })
        .then(response => response.json())
        .then(data => {
            if (data.status === 'success') {
                console.log('Onboarding completed successfully');

                // Show success message
                completeBtn.innerHTML = '<i class="fas fa-check mr-2"></i>Complete!';
                completeBtn.classList.remove('btn-success');
                completeBtn.classList.add('btn-primary');

                // Clear onboarding cookies
                clearOnboardingCookies();

                // Close modal and clean up URL
                setTimeout(() => {
                    $('#onboardingModal').modal('hide');

                    // Remove onboarding parameter from URL
                    const url = new URL(window.location);
                    url.searchParams.delete('onboarding');
                    window.history.replaceState({}, document.title, url.pathname);

                    // Refresh page to reflect updated user info
                    window.location.reload();
                }, 1500);

            } else {
                showOnboardingError(data.message || 'An error occurred while completing your profile.');
                completeBtn.disabled = false;
                completeBtn.innerHTML = '<i class="fas fa-check mr-2"></i>Complete Profile';
            }
        })
        .catch(error => {
            console.error('Onboarding error:', error);
            showOnboardingError('A server error occurred. Please try again.');
            completeBtn.disabled = false;
            completeBtn.innerHTML = '<i class="fas fa-check mr-2"></i>Complete Profile';
        });
}

// Show onboarding error message
function showOnboardingError(message) {
    const errorElement = document.getElementById('onboarding-error');
    if (errorElement) {
        errorElement.textContent = message;
        errorElement.classList.remove('d-none');

        // Hide error message after 5 seconds
        setTimeout(() => {
            errorElement.classList.add('d-none');
        }, 5000);
    }
}

// Clear onboarding-related cookies
function clearOnboardingCookies() {
    // Set cookies to expire immediately
    document.cookie = 'is_new_user=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
    document.cookie = 'user_id=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;';
    console.log('Cleared onboarding cookies');
}

// Get cookie value (utility function)
function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) {
        return parts.pop().split(';').shift();
    }
    return null;
}