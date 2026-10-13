// 디자인 확인용 샘플. 실제 구현 시 Mapper 조회 결과로 교체
const communityDesignData = {
  "recipes": [
    {
      "recipeId": 1,
      "title": "토마토 바질 파스타",
      "summary": "상큼한 토마토와 바질로 만드는, 부담 없는 한 끼.",
      "category": "양식",
      "authorName": "한끼요리",
      "authorId": 1,
      "cookMinutes": 20,
      "difficulty": "쉬움",
      "servings": 2,
      "likes": 19,
      "bookmarks": 6,
      "image": "food-1.svg",
      "tags": [
        "간단요리",
        "자취요리"
      ],
      "ingredients": [
        {
          "name": "스파게티 면",
          "amount": 160,
          "unit": "g",
          "ingredientId": 1
        },
        {
          "name": "토마토",
          "amount": 2,
          "unit": "개",
          "ingredientId": 2
        },
        {
          "name": "올리브유",
          "amount": 1,
          "unit": "큰술",
          "ingredientId": 3
        }
      ],
      "steps": [
        {
          "description": "재료를 씻고 손질해 주세요. 채소는 먹기 좋은 크기로 썰어 준비합니다.",
          "image": "food-1.svg",
          "timerSeconds": 0
        },
        {
          "description": "팬을 예열하고 준비한 재료를 넣어 조리해 주세요. 타지 않도록 중간중간 잘 섞어 줍니다.",
          "image": "food-1.svg",
          "timerSeconds": 120
        },
        {
          "description": "간을 확인하고 그릇에 담아 완성합니다. 따뜻할 때 맛있게 드세요.",
          "image": "food-1.svg",
          "timerSeconds": 0
        }
      ]
    },
    {
      "recipeId": 2,
      "title": "버섯 크림 파스타",
      "summary": "버섯의 향을 담아 부드럽고 고소하게 완성해요.",
      "category": "양식",
      "authorName": "따뜻한주방",
      "authorId": 2,
      "cookMinutes": 25,
      "difficulty": "쉬움",
      "servings": 2,
      "likes": 26,
      "bookmarks": 8,
      "image": "food-2.svg",
      "tags": [
        "한끼식사",
        "간단요리"
      ],
      "ingredients": [
        {
          "name": "스파게티 면",
          "amount": 160,
          "unit": "g",
          "ingredientId": 1
        },
        {
          "name": "양파",
          "amount": 0.5,
          "unit": "개",
          "ingredientId": 5
        },
        {
          "name": "올리브유",
          "amount": 1,
          "unit": "큰술",
          "ingredientId": 3
        }
      ],
      "steps": [
        {
          "description": "재료를 씻고 손질해 주세요. 채소는 먹기 좋은 크기로 썰어 준비합니다.",
          "image": "food-2.svg",
          "timerSeconds": 0
        },
        {
          "description": "팬을 예열하고 준비한 재료를 넣어 조리해 주세요. 타지 않도록 중간중간 잘 섞어 줍니다.",
          "image": "food-2.svg",
          "timerSeconds": 120
        },
        {
          "description": "간을 확인하고 그릇에 담아 완성합니다. 따뜻할 때 맛있게 드세요.",
          "image": "food-2.svg",
          "timerSeconds": 0
        }
      ]
    },
    {
      "recipeId": 3,
      "title": "달걀 채소 볶음밥",
      "summary": "냉장고 속 채소로 알록달록한 한 접시를 만들어요.",
      "category": "한식",
      "authorName": "한끼요리",
      "authorId": 1,
      "cookMinutes": 15,
      "difficulty": "쉬움",
      "servings": 2,
      "likes": 33,
      "bookmarks": 10,
      "image": "food-3.svg",
      "tags": [
        "남은재료",
        "자취요리"
      ],
      "ingredients": [
        {
          "name": "달걀",
          "amount": 2,
          "unit": "개",
          "ingredientId": 4
        },
        {
          "name": "양파",
          "amount": 0.5,
          "unit": "개",
          "ingredientId": 5
        },
        {
          "name": "올리브유",
          "amount": 1,
          "unit": "큰술",
          "ingredientId": 3
        }
      ],
      "steps": [
        {
          "description": "재료를 씻고 손질해 주세요. 채소는 먹기 좋은 크기로 썰어 준비합니다.",
          "image": "food-3.svg",
          "timerSeconds": 0
        },
        {
          "description": "팬을 예열하고 준비한 재료를 넣어 조리해 주세요. 타지 않도록 중간중간 잘 섞어 줍니다.",
          "image": "food-3.svg",
          "timerSeconds": 120
        },
        {
          "description": "간을 확인하고 그릇에 담아 완성합니다. 따뜻할 때 맛있게 드세요.",
          "image": "food-3.svg",
          "timerSeconds": 0
        }
      ]
    },
    {
      "recipeId": 4,
      "title": "두부 달걀 덮밥",
      "summary": "부드러운 두부와 달걀로 간단하고 든든하게.",
      "category": "한식",
      "authorName": "오늘의밥상",
      "authorId": 3,
      "cookMinutes": 15,
      "difficulty": "쉬움",
      "servings": 2,
      "likes": 40,
      "bookmarks": 12,
      "image": "food-4.svg",
      "tags": [
        "건강요리",
        "한끼식사"
      ],
      "ingredients": [
        {
          "name": "달걀",
          "amount": 2,
          "unit": "개",
          "ingredientId": 4
        },
        {
          "name": "양파",
          "amount": 0.5,
          "unit": "개",
          "ingredientId": 5
        },
        {
          "name": "올리브유",
          "amount": 1,
          "unit": "큰술",
          "ingredientId": 3
        }
      ],
      "steps": [
        {
          "description": "재료를 씻고 손질해 주세요. 채소는 먹기 좋은 크기로 썰어 준비합니다.",
          "image": "food-4.svg",
          "timerSeconds": 0
        },
        {
          "description": "팬을 예열하고 준비한 재료를 넣어 조리해 주세요. 타지 않도록 중간중간 잘 섞어 줍니다.",
          "image": "food-4.svg",
          "timerSeconds": 120
        },
        {
          "description": "간을 확인하고 그릇에 담아 완성합니다. 따뜻할 때 맛있게 드세요.",
          "image": "food-4.svg",
          "timerSeconds": 0
        }
      ]
    },
    {
      "recipeId": 5,
      "title": "딸기 프렌치토스트",
      "summary": "주말 아침을 달콤하게 여는 작은 브런치.",
      "category": "간식",
      "authorName": "따뜻한주방",
      "authorId": 2,
      "cookMinutes": 15,
      "difficulty": "쉬움",
      "servings": 2,
      "likes": 47,
      "bookmarks": 14,
      "image": "food-5.svg",
      "tags": [
        "브런치",
        "간단요리"
      ],
      "ingredients": [
        {
          "name": "달걀",
          "amount": 2,
          "unit": "개",
          "ingredientId": 4
        },
        {
          "name": "양파",
          "amount": 0.5,
          "unit": "개",
          "ingredientId": 5
        },
        {
          "name": "올리브유",
          "amount": 1,
          "unit": "큰술",
          "ingredientId": 3
        }
      ],
      "steps": [
        {
          "description": "재료를 씻고 손질해 주세요. 채소는 먹기 좋은 크기로 썰어 준비합니다.",
          "image": "food-5.svg",
          "timerSeconds": 0
        },
        {
          "description": "팬을 예열하고 준비한 재료를 넣어 조리해 주세요. 타지 않도록 중간중간 잘 섞어 줍니다.",
          "image": "food-5.svg",
          "timerSeconds": 120
        },
        {
          "description": "간을 확인하고 그릇에 담아 완성합니다. 따뜻할 때 맛있게 드세요.",
          "image": "food-5.svg",
          "timerSeconds": 0
        }
      ]
    },
    {
      "recipeId": 6,
      "title": "고구마 맛탕",
      "summary": "겉은 바삭하고 속은 촉촉한 오늘의 간식.",
      "category": "간식",
      "authorName": "초록식탁",
      "authorId": 4,
      "cookMinutes": 30,
      "difficulty": "쉬움",
      "servings": 2,
      "likes": 54,
      "bookmarks": 16,
      "image": "food-6.svg",
      "tags": [
        "간식",
        "남은재료"
      ],
      "ingredients": [
        {
          "name": "달걀",
          "amount": 2,
          "unit": "개",
          "ingredientId": 4
        },
        {
          "name": "양파",
          "amount": 0.5,
          "unit": "개",
          "ingredientId": 5
        },
        {
          "name": "올리브유",
          "amount": 1,
          "unit": "큰술",
          "ingredientId": 3
        }
      ],
      "steps": [
        {
          "description": "재료를 씻고 손질해 주세요. 채소는 먹기 좋은 크기로 썰어 준비합니다.",
          "image": "food-6.svg",
          "timerSeconds": 0
        },
        {
          "description": "팬을 예열하고 준비한 재료를 넣어 조리해 주세요. 타지 않도록 중간중간 잘 섞어 줍니다.",
          "image": "food-6.svg",
          "timerSeconds": 120
        },
        {
          "description": "간을 확인하고 그릇에 담아 완성합니다. 따뜻할 때 맛있게 드세요.",
          "image": "food-6.svg",
          "timerSeconds": 0
        }
      ]
    }
  ],
  "reviews": [
    {
      "reviewId": 1,
      "recipeId": 1,
      "title": "첫 파스타, 생각보다 쉽게 성공했어요!",
      "authorName": "오늘의밥상",
      "authorId": 3,
      "content": "요리모드로 한 단계씩 따라 하니 어렵지 않았어요. 바질은 조금만 넣었는데도 향이 정말 좋네요. 다음에는 가족들에게도 만들어 주려고요.",
      "image": "food-1.svg",
      "originalActive": true
    },
    {
      "reviewId": 2,
      "recipeId": 3,
      "title": "남은 채소가 맛있는 볶음밥이 됐어요.",
      "authorName": "초록식탁",
      "authorId": 4,
      "content": "냉장고에 있던 당근과 양파를 넣었어요. 간단하지만 든든해서 자주 만들 것 같아요.",
      "image": "food-3.svg",
      "originalActive": true
    },
    {
      "reviewId": 3,
      "recipeId": 6,
      "title": "주말 간식으로 고구마 맛탕!",
      "authorName": "한끼요리",
      "authorId": 1,
      "content": "설탕 양을 조금 줄였는데도 충분히 달콤했어요. 아이들과 맛있게 먹었습니다.",
      "image": "food-6.svg",
      "originalActive": false
    }
  ],
  "questions": [
    {
      "questionId": 1,
      "title": "파스타 면수는 얼마나 넣어야 하나요?",
      "authorName": "오늘의밥상",
      "authorId": 3,
      "content": "토마토 파스타를 만들고 있는데 소스가 조금 뻑뻑해요. 면수를 한 번에 많이 넣어도 될까요? 2인분 기준으로 어느 정도가 적당한지 궁금합니다.",
      "date": "2026.10.12",
      "answers": [
        {
          "answerId": 1,
          "authorName": "한끼요리",
          "owner": false,
          "content": "한 번에 많이 넣기보다는 한두 큰술씩 넣으며 농도를 확인해 보세요. 면을 소스와 함께 섞으면 더 잘 어우러져요."
        },
        {
          "answerId": 2,
          "authorName": "오늘의밥상",
          "owner": true,
          "content": "조금씩 넣으니 훨씬 잘 섞이네요. 감사합니다!"
        }
      ]
    },
    {
      "questionId": 2,
      "title": "남은 두부는 어떻게 보관하면 좋을까요?",
      "authorName": "초록식탁",
      "authorId": 4,
      "content": "두부를 반 모만 사용했어요. 남은 두부를 신선하게 보관하는 방법이 궁금해요.",
      "date": "2026.10.11",
      "answers": []
    },
    {
      "questionId": 3,
      "title": "볶음밥이 질어지지 않게 만드는 방법이 있나요?",
      "authorName": "따뜻한주방",
      "authorId": 2,
      "content": "볶음밥을 하면 밥알이 잘 떨어지지 않고 질어져요. 어떤 순서로 볶으면 좋을까요?",
      "date": "2026.10.10",
      "answers": [
        {
          "answerId": 3,
          "authorName": "한끼요리",
          "owner": false,
          "content": "채소의 수분을 먼저 날리고 밥을 넣어 볶아보세요."
        }
      ]
    }
  ],
  "chefs": [
    {
      "memberId": 1,
      "name": "한끼요리",
      "intro": "익숙한 재료로 만드는 새로운 한 끼",
      "recipeCount": 18,
      "likeCount": 248,
      "image": "chef-1.svg"
    },
    {
      "memberId": 2,
      "name": "따뜻한주방",
      "intro": "마음을 담아 만드는 집밥과 브런치",
      "recipeCount": 15,
      "likeCount": 216,
      "image": "chef-2.svg"
    },
    {
      "memberId": 3,
      "name": "오늘의밥상",
      "intro": "오늘도 맛있고 든든하게",
      "recipeCount": 12,
      "likeCount": 173,
      "image": "chef-3.svg"
    },
    {
      "memberId": 4,
      "name": "초록식탁",
      "intro": "남은 재료도 소중하게 사용해요",
      "recipeCount": 9,
      "likeCount": 128,
      "image": "chef-4.svg"
    }
  ],
  "ingredients": [
    {
      "ingredientId": 1,
      "name": "스파게티 면",
      "unit": "g"
    },
    {
      "ingredientId": 2,
      "name": "토마토",
      "unit": "개"
    },
    {
      "ingredientId": 3,
      "name": "올리브유",
      "unit": "큰술"
    },
    {
      "ingredientId": 4,
      "name": "달걀",
      "unit": "개"
    },
    {
      "ingredientId": 5,
      "name": "양파",
      "unit": "개"
    },
    {
      "ingredientId": 6,
      "name": "버섯",
      "unit": "g"
    },
    {
      "ingredientId": 7,
      "name": "두부",
      "unit": "모"
    },
    {
      "ingredientId": 8,
      "name": "소금",
      "unit": "작은술"
    },
    {
      "ingredientId": 9,
      "name": "고구마",
      "unit": "개"
    },
    {
      "ingredientId": 10,
      "name": "딸기",
      "unit": "개"
    }
  ],
  "categories": [
    "한식",
    "양식",
    "간식"
  ],
  "tags": [
    "간단요리",
    "자취요리",
    "한끼식사",
    "남은재료",
    "건강요리",
    "브런치",
    "간식"
  ]
};
