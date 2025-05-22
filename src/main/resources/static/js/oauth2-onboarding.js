// src/main/resources/static/js/oauth2-onboarding.js
document.addEventListener('DOMContentLoaded', function() {
    // 온보딩 상태 확인
    checkOnboardingStatus();

    // URL 파라미터에서 onboarding 확인
    const urlParams = new URLSearchParams(window.location.search);
    if (urlParams.get('onboarding') === 'true') {
        showOnboardingModal();
    }
});

// 온보딩 상태 확인
function checkOnboardingStatus() {
    fetch('/api/oauth2/onboarding/status')
        .then(response => response.json())
        .then(data => {
            if (data.status === 'success' && data.data.needsAdditionalInfo) {
                console.log('User needs additional info:', data.data);
                showOnboardingModal(data.data);
            }
        })
        .catch(error => {
            console.error('Error checking onboarding status:', error);
        });
}

// 온보딩 모달 표시 (HTML 생성 대신 기존 모달 사용)
function showOnboardingModal(userData = null) {
    // 사용자 데이터가 있으면 폼에 채우기
    if (userData && userData.userId) {
        document.getElementById('onboarding-user-id').value = userData.userId;
    }

    // 완료 버튼 이벤트 리스너 추가 (기존에 없다면)
    const completeBtn = document.getElementById('complete-onboarding-btn');
    if (completeBtn && !completeBtn.hasAttribute('data-listener-added')) {
        completeBtn.addEventListener('click', completeOnboarding);
        completeBtn.setAttribute('data-listener-added', 'true');
    }

    // 모달 표시
    $('#onboardingModal').modal({
        backdrop: 'static',
        keyboard: false
    });
}
// 온보딩 완료 처리
function completeOnboarding() {
    const form = document.getElementById('onboarding-form');
    const formData = new FormData(form);
    const completionData = {};

    // FormData를 객체로 변환
    for (let [key, value] of formData.entries()) {
        if (value.trim() !== '') {
            if (key === 'userId') {
                completionData[key] = parseInt(value);
            } else {
                completionData[key] = value;
            }
        }
    }

    // 필수 필드 확인
    if (!completionData.userId || !completionData.gender || !completionData.birthday) {
        showOnboardingError('성별과 생년월일은 필수 입력 항목입니다.');
        return;
    }

    const completeBtn = document.getElementById('complete-onboarding-btn');
    completeBtn.disabled = true;
    completeBtn.innerHTML = '<i class="fas fa-spinner fa-spin mr-2"></i>처리 중...';

    // 서버에 데이터 전송
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

                // 성공 메시지 표시
                completeBtn.innerHTML = '<i class="fas fa-check mr-2"></i>완료!';
                completeBtn.classList.remove('btn-success');
                completeBtn.classList.add('btn-primary');

                // 모달 닫기 및 페이지 새로고침
                setTimeout(() => {
                    $('#onboardingModal').modal('hide');
                    // URL에서 onboarding 파라미터 제거
                    const url = new URL(window.location);
                    url.searchParams.delete('onboarding');
                    window.history.replaceState({}, document.title, url.pathname);

                    // 페이지 새로고침으로 업데이트된 사용자 정보 반영
                    window.location.reload();
                }, 1500);

            } else {
                showOnboardingError(data.message || '정보 입력 중 오류가 발생했습니다.');
                completeBtn.disabled = false;
                completeBtn.innerHTML = '<i class="fas fa-check mr-2"></i>정보 입력 완료';
            }
        })
        .catch(error => {
            console.error('Onboarding error:', error);
            showOnboardingError('서버 오류가 발생했습니다. 다시 시도해주세요.');
            completeBtn.disabled = false;
            completeBtn.innerHTML = '<i class="fas fa-check mr-2"></i>정보 입력 완료';
        });
}

// 온보딩 오류 메시지 표시
function showOnboardingError(message) {
    const errorElement = document.getElementById('onboarding-error');
    errorElement.textContent = message;
    errorElement.classList.remove('d-none');

    // 3초 후 오류 메시지 숨기기
    setTimeout(() => {
        errorElement.classList.add('d-none');
    }, 5000);
}

// 쿠키에서 값 가져오기 (디버깅 용도)
function getCookie(name) {
    const value = `; ${document.cookie}`;
    const parts = value.split(`; ${name}=`);
    if (parts.length === 2) return parts.pop().split(';').shift();
    return null;
}