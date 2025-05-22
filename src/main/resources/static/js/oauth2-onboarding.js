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

// 온보딩 모달 표시
function showOnboardingModal(userData = null) {
    // 모달이 이미 존재하는지 확인
    let modal = document.getElementById('onboardingModal');
    if (!modal) {
        modal = createOnboardingModal();
        document.body.appendChild(modal);
    }

    // 사용자 데이터가 있으면 폼에 채우기
    if (userData && userData.userId) {
        document.getElementById('onboarding-user-id').value = userData.userId;
    }

    // 모달 표시
    $('#onboardingModal').modal({
        backdrop: 'static',
        keyboard: false
    });
}

// 온보딩 모달 HTML 생성
function createOnboardingModal() {
    const modalHTML = `
    <div class="modal fade" id="onboardingModal" tabindex="-1" aria-labelledby="onboardingModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-lg">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="onboardingModalLabel">
                        <i class="fas fa-user-plus mr-2"></i>추가 정보 입력
                    </h5>
                </div>
                <div class="modal-body">
                    <div class="alert alert-info">
                        <i class="fas fa-info-circle mr-2"></i>
                        서비스 이용을 위해 추가 정보를 입력해주세요.
                    </div>
                    <form id="onboarding-form">
                        <input type="hidden" id="onboarding-user-id" name="userId">
                        
                        <div class="form-row">
                            <div class="form-group col-md-6">
                                <label for="onboarding-gender">성별 <span class="text-danger">*</span></label>
                                <select class="form-control" id="onboarding-gender" name="gender" required>
                                    <option value="">성별을 선택하세요</option>
                                    <option value="M">남성</option>
                                    <option value="F">여성</option>
                                    <option value="O">기타</option>
                                </select>
                            </div>
                            <div class="form-group col-md-6">
                                <label for="onboarding-birthday">생년월일 <span class="text-danger">*</span></label>
                                <input type="date" class="form-control" id="onboarding-birthday" name="birthday" required>
                            </div>
                        </div>
                        
                        <div class="form-group">
                            <label for="onboarding-nickname">닉네임 변경 (선택사항)</label>
                            <input type="text" class="form-control" id="onboarding-nickname" name="nickname" 
                                   placeholder="닉네임을 변경하려면 입력하세요">
                            <small class="form-text text-muted">입력하지 않으면 기존 닉네임을 유지합니다.</small>
                        </div>
                        
                        <div class="form-group">
                            <label for="onboarding-notion-page-id">노션 페이지 ID (선택사항)</label>
                            <input type="text" class="form-control" id="onboarding-notion-page-id" name="notionPageId" 
                                   placeholder="노션 페이지 ID를 입력하세요 (선택사항)">
                            <small class="form-text text-muted">노션과 연동하려면 페이지 ID를 입력하세요.</small>
                        </div>
                        
                        <div class="alert alert-danger d-none" id="onboarding-error"></div>
                    </form>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-success btn-block" id="complete-onboarding-btn">
                        <i class="fas fa-check mr-2"></i>정보 입력 완료
                    </button>
                </div>
            </div>
        </div>
    </div>`;

    const modalElement = document.createElement('div');
    modalElement.innerHTML = modalHTML;

    // 완료 버튼 이벤트 리스너 추가
    modalElement.querySelector('#complete-onboarding-btn').addEventListener('click', completeOnboarding);

    return modalElement.firstElementChild;
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