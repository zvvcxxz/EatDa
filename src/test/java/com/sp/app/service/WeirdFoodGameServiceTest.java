package com.sp.app.service;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
class WeirdFoodGameServiceTest {
 @Test void validatesSentence() {
  var builder=mock(ChatClient.Builder.class); when(builder.build()).thenReturn(mock(ChatClient.class));
  var service=new WeirdFoodGameService(builder);
  assertEquals("김치 파인애플로 만들어줘", service.validatePrompt(" 김치 파인애플로 만들어줘 "));
  assertThrows(IllegalArgumentException.class, () -> service.validatePrompt(" "));
  service.close();
 }
 @Test void rejectsInvalidVerdictAndDuplicates() {
  assertThrows(IllegalArgumentException.class, () -> WeirdFoodGameService.result(new WeirdFoodGameService.Verdict(false,"",null,null,null,null,null,null)));
  assertThrows(WeirdFoodGameService.JudgeUnavailableException.class, () -> WeirdFoodGameService.result(new WeirdFoodGameService.Verdict(true,"",List.of("김치","김치"),"조합",60,"예상 맛","심사평","완성 사진")));
 }
 @Test void returnsParsedIngredients() {
  var result=WeirdFoodGameService.result(new WeirdFoodGameService.Verdict(true,"",List.of("김치","파인애플"),"김치 파인애플",85,"새콤할 것 같아요","상상 초월","과일과 김치가 담긴 접시"));
  assertEquals(2,result.ingredients().size()); assertEquals("상상 초월 조합",result.grade());
 }
}
