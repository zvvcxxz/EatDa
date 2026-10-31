package com.sp.app.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import com.sp.app.domain.dto.MyRecipeAiDto;
import com.sp.app.postgres.mapper.MyRecipeAiMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class MyRecipeAiServiceImpl implements MyRecipeAiService {
	private final MyRecipeAiMapper recipeMapper;
	private final EmbeddingModel embeddingModel;
	
	@Override
	public MyRecipeAiDto findById(String rcp_seq) {
		MyRecipeAiDto dto = null;
		
		try {
			dto = recipeMapper.findById(rcp_seq);
		} catch (Exception e) {
			log.info("findById : ", e);
		}
		
		return dto;
	}

	@Override
	public List<Map<String, Object>> recommendRecipes(List<String> ingredients) {
		// 재료를 이용한 레시피 유사 검색
		try {
			String textIngredients = convertIngredients(ingredients);
			if(textIngredients.isBlank()) {
				 return Collections.emptyList();
			}
			
			// 레시피를 Embedding으로 변환
			float[] vector = embeddingModel.embed(textIngredients);
			String vectorString = convertVectorToString(vector);
			
			// PostgreSQL pgVector 유사 검색
			List<Map<String, Object>> recipes = 
					recipeMapper.findBySimilarRecipes(vectorString, 8);
			
			
			// 사용자가 보유한 재료 정규화
			Set<String> ownedIngredients = normalizeOwnedIngredients(ingredients);
			
			for (Map<String, Object> recipe : recipes) {
				 Object parts = recipe.get("rcp_parts_dtls");
				 
				 String partsText = parts == null ? "" : parts.toString();
				 
				 // 레시피에 필요한 전체 재료
				 List<String> requiredIngredients = parseIngredients(partsText);

				 // 존재하는 재료
				 List<String> haveIngredients = calculateHaveIngredients(requiredIngredients, ownedIngredients);

				 // 부족한 재료
				 List<String> missingIngredients = calculateMissingIngredients(requiredIngredients, ownedIngredients);

				 // 존재율
				 int matchRate = calculateMatchRate(requiredIngredients, haveIngredients);

				 recipe.put("haveIngredients", haveIngredients);
				 recipe.put("missingIngredients", missingIngredients);
				 recipe.put("matchRate", matchRate);				 
			}
			
			return recipes;
			
		} catch (Exception e) {
			log.info("recommendRecipes : ", e);
			
			return Collections.emptyList();
		}
	}
	
	@Override
	public List<Map<String, Object>> recommendRecipes(String keyword) {
		// 검색 문자열을 이용한 레시피 유사 검색
		// 검색 문자열 예 : 돼지고기 두부 김치 대파 양파를 이용할 수 있는 레시피
		try {
			if(keyword == null || keyword.isBlank()) {
				 return Collections.emptyList();
			}
			
			// 레시피를 Embedding으로 변환
			float[] vector = embeddingModel.embed(keyword);
			String vectorString = convertVectorToString(vector);
			
			// PostgreSQL pgVector 유사 검색
			List<Map<String, Object>> recipes =
					recipeMapper.findBySimilarRecipes(vectorString, 6);

			return recipes;			
		} catch (Exception e) {
			log.info("recommendRecipes : ", e);
			
			return Collections.emptyList();
		}
	}
	
	// List를 "달걀 돼지고기 양파" 처럼 문자열로 변환 
	private String convertIngredients(List<String> ingredients) {
		if (ingredients == null || ingredients.isEmpty()) {
			return "";
		}

		// Arrays.stream() : 나뉜 문자열 배열을 Stream으로 변환
		// flatMap() : 나뉜 결과들을 하나의 Stream으로 합친다.
		// map(String::trim) : 각 재료의 앞뒤 공백 제거
		return ingredients.stream()
				.filter(s -> s != null && !s.trim().isEmpty())
				.flatMap(s -> Arrays.stream(s.split("\\|")))
				.map(String::trim)
				.filter(s -> !s.isEmpty())
				.map(s -> s.replaceAll("\\s+", ""))
				.collect(Collectors.joining(" "));
	}
	
	// float[] -> pgvector 문자열로 변환
	// pgVector에서는 다음 형태를 사용 : [0.1,0.2,0.3,...]
	private String convertVectorToString(float[] vector) {
        String embedding = Arrays.stream(toDoubleArray(vector))  // float[]를 double[]로 변환
        		.mapToObj(Double::toString)  // 각 숫자를 문자열로 변환
        		.collect(Collectors.joining(",", "[", "]")); // 문자열들을 하나로 합칩다. 
        				// 첫 번째 ",": 각 값 사이에 넣을 구분자
        				// 두 번째 "[": 문자열의 시작 부분
        				// 세 번째 "]": 문자열의 끝 부분
        
        return embedding;
	}	

	private double[] toDoubleArray(float[] vector) {
		double[] result = new double[vector.length];

		for (int i = 0; i < vector.length; i++) {
			result[i] = vector[i];
		}

		return result;
	}
	
	// 재료 정보에서 섹션 제목, 수량, 괄호 내용을 제거
	private List<String> parseIngredients(String parts) {
		if (parts == null || parts.trim().isEmpty()) {
			return Collections.emptyList();
		}
		
		// 괄호 안의 중량, 수량 등 제거
	    final Pattern PARENTHESES = Pattern.compile("\\([^)]*\\)|（[^）]*）");

	    // 숫자와 단위 및 그 뒤의 내용 제거
	    final Pattern QUANTITY = Pattern.compile(
			"\\s*\\d+(?:\\.\\d+)?(?:\\s*/\\s*\\d+)?"
			+ "[½¼¾⅓⅔⅛⅜⅝⅞]?"
			+ "\\s*(?:kg|mg|ml|g|l|개|모|컵|큰술|작은술|"
			+ "줄기|줄|쪽|장|통|cm|mm|스푼|줌|꼬집|알|"
			+ "마리|팩|봉|캔|토막|근|포기|뿌리|대|T|t|Ts)"
			+ ".*$",
			Pattern.CASE_INSENSITIVE
		);

	   // 약간, 적당량 등의 표현
	   final Pattern AMOUNT_WORDS = Pattern.compile("(약간|적당량|적당히|조금|취향껏)$");		
		
	   // [양념장] 같은 섹션 제목 제거
	   String text = parts.replaceAll("\\[[^\\]]*\\]", "\n");

	   // 괄호 안의 정량 및 설명 제거
	   text = PARENTHESES.matcher(text).replaceAll("");
       
	   // 쉼표, 줄바꿈으로 분리
	   String[] items = text.split("[,\\n\\r]+");
	
	   Set<String> result = new LinkedHashSet<>();
	   for (String item : items) {
		   String name = item.trim();

		   // 건포도 5알, 호두 1/2알, 두유 50g 등의 수량 제거
		   name = QUANTITY.matcher(name).replaceFirst("");

		   // 소금 약간, 설탕 약간 등의 표현 제거
		   name = AMOUNT_WORDS.matcher(name).replaceFirst("");
		   name = name.trim();

		   if (!name.isEmpty()) {
			   result.add(name);
		   }
	   }

	   return new ArrayList<>(result);
	}
	
	
	// 보유 재료 정규화(계란|달걀과 같은 동의어도 각각 등록)
	private Set<String> normalizeOwnedIngredients(
			List<String> ingredients) {

		if (ingredients == null || ingredients.isEmpty()) {
			return Collections.emptySet();
		}

		return ingredients.stream()
				.filter(Objects::nonNull)
				.flatMap(s -> Arrays.stream(s.split("\\|")))
				.map(this::normalize)
				.filter(s -> !s.isEmpty())
				.collect(Collectors.toCollection(LinkedHashSet::new));
    }
	
	// 재료 비교용 정규화
    private String normalize(String ingredient) {
    	if (ingredient == null) {
    		return "";
    	}

    	return ingredient.replaceAll("\\s+", "").toLowerCase();
	}
	
    // 보유하고 있는 레시피 재료 목록
	private List<String> calculateHaveIngredients(
			List<String> requiredIngredients,
			Set<String> ownedIngredients) {

		return requiredIngredients.stream()
				.filter(item -> ownedIngredients.contains(normalize(item)))
				.collect(Collectors.toList());
    }

	// 보유하지 않은 레시피 재료 목록
	private List<String> calculateMissingIngredients(
			List<String> requiredIngredients,
			Set<String> ownedIngredients) {

		return requiredIngredients.stream()
                .filter(item -> !ownedIngredients.contains(normalize(item)))
                .collect(Collectors.toList());
	}

	// 재료 일치율 계산 (0~100 정수 백분율)
	private int calculateMatchRate(
			List<String> requiredIngredients,
			List<String> haveIngredients) {

		if (requiredIngredients == null || requiredIngredients.isEmpty()) {
			return 0;
		}

		return (int) Math.round(haveIngredients.size() * 100.0 / requiredIngredients.size());
    }    
}
