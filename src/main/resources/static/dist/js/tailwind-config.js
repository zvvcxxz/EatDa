tailwind.config = {
            theme: {
                extend: {
                    colors: {
                        brand: {
                            red: '#E0533C',      // 식욕 자극 테라코타 레드
                            orange: '#FF6B4A',   // 따뜻한 주황
                            yellow: '#F5A623',   // 노랑
                            cream: '#FFFDF7',    // 따뜻한 크림 흰색
                            dark: '#2C221E',     // 다크 에스프레소
                            green: '#3D7B66',    // 신선한 야채 그린 (로고 포인트)
                            softYellow: '#FFF7E6',
                            softRed: '#FFF0ED'
                        }
                    },
                    fontFamily: {
                        sans: ['Noto Sans KR', 'sans-serif'],
                        outfit: ['Outfit', 'sans-serif']
                    },
                    keyframes: {
                        pulseGlow: {
                            '0%, 100%': { transform: 'scale(1)', opacity: '1' },
                            '50%': { transform: 'scale(1.05)', opacity: '0.85' }
                        },
                        floatSlow: {
                            '0%, 100%': { transform: 'translateY(0px)' },
                            '50%': { transform: 'translateY(-8px)' }
                        }
                    },
                    animation: {
                        pulseGlow: 'pulseGlow 2.5s infinite ease-in-out',
                        floatSlow: 'floatSlow 4s infinite ease-in-out'
                    }
                }
            }
        }
