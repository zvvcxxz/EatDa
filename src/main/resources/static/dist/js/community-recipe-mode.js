// communityRecipe-mode.html 전용 동작

document.addEventListener('DOMContentLoaded', function() {
    const stages = Array.from(
        document.querySelectorAll('.cooking-stage')
    );

    if (stages.length === 0) return;

    const previousButton = document.getElementById('previousStep');
    const nextButton = document.getElementById('nextStep');
    const finishLink = document.getElementById('finishCooking');
    const progress = document.getElementById('cookingProgress');
    const stepCount = document.getElementById('stepCount');

    let currentStep = 0;

    // 단계마다 남은 시간을 따로 보관
    const timers = Array.from(
        document.querySelectorAll('.cooking-timer')
    ).map(function(element) {
        const seconds = Number(element.dataset.seconds);

        return {
            element: element,
            originalSeconds: seconds,
            remaining: seconds,
            endTime: null,
            intervalId: null
        };
    });

    function formatTime(seconds) {
        const minutes = Math.floor(seconds / 60);
        const rest = seconds % 60;

        return String(minutes).padStart(2, '0')
            + ':' + String(rest).padStart(2, '0');
    }

    function updateClock(timer) {
        timer.element.querySelector('.cooking-clock').textContent =
            formatTime(timer.remaining);
    }

    function pauseTimer(timer) {
        if (timer.intervalId === null) return;

        timer.remaining = Math.max(
            0,
            Math.ceil((timer.endTime - Date.now()) / 1000)
        );

        clearInterval(timer.intervalId);
        timer.intervalId = null;
        timer.endTime = null;

        updateClock(timer);

        timer.element.querySelector('[data-timer-start]')
            .textContent = '시작';

        timer.element.querySelector('.cooking-timer-message')
            .textContent = timer.remaining === 0
                ? '시간이 다 됐어요!'
                : '일시정지';
    }

    timers.forEach(function(timer) {
        const startButton = timer.element.querySelector(
            '[data-timer-start]'
        );

        const resetButton = timer.element.querySelector(
            '[data-timer-reset]'
        );

        const message = timer.element.querySelector(
            '.cooking-timer-message'
        );

        updateClock(timer);

        startButton.addEventListener('click', function() {
            if (timer.intervalId !== null) {
                pauseTimer(timer);
                return;
            }

            if (timer.remaining === 0) {
                timer.remaining = timer.originalSeconds;
            }

            timer.endTime = Date.now() + timer.remaining * 1000;
            startButton.textContent = '일시정지';
            message.textContent = '진행 중';

            timer.intervalId = setInterval(function() {
                timer.remaining = Math.max(
                    0,
                    Math.ceil((timer.endTime - Date.now()) / 1000)
                );

                updateClock(timer);

                if (timer.remaining === 0) {
                    clearInterval(timer.intervalId);
                    timer.intervalId = null;
                    timer.endTime = null;

                    startButton.textContent = '다시 시작';
                    message.textContent = '시간이 다 됐어요!';
                }
            }, 250);
        });

        resetButton.addEventListener('click', function() {
            pauseTimer(timer);

            timer.remaining = timer.originalSeconds;
            updateClock(timer);

            startButton.textContent = '시작';
            message.textContent = '준비';
        });
    });

    function showStep() {
        stages.forEach(function(stage, index) {
            stage.hidden = index !== currentStep;
        });

        stepCount.textContent =
            (currentStep + 1) + ' / ' + stages.length + ' 단계';

        progress.value = currentStep + 1;
        previousButton.disabled = currentStep === 0;

        const lastStep = currentStep === stages.length - 1;

        nextButton.hidden = lastStep;
        finishLink.hidden = !lastStep;
    }

    // 다른 단계로 이동하면 진행 중인 타이머를 멈춤
    function moveStep(direction) {
        timers.forEach(pauseTimer);

        currentStep = Math.max(
            0,
            Math.min(currentStep + direction, stages.length - 1)
        );

        showStep();
    }

    previousButton.addEventListener('click', function() {
        moveStep(-1);
    });

    nextButton.addEventListener('click', function() {
        moveStep(1);
    });

    showStep();
});
