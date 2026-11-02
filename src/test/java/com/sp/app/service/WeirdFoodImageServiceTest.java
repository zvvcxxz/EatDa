package com.sp.app.service;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;
class WeirdFoodImageServiceTest {
 @Test void rejectsTextOnlyAndInvalidImage() {
  var mapper=JsonMapper.builder().build();
  assertThrows(WeirdFoodGameService.JudgeUnavailableException.class, () -> WeirdFoodImageService.picture(mapper.readTree("{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"no image\"}]}}]}")));
  assertThrows(WeirdFoodGameService.JudgeUnavailableException.class, () -> WeirdFoodImageService.picture(mapper.readTree("{\"candidates\":[{\"content\":{\"parts\":[{\"inlineData\":{\"mimeType\":\"image/png\",\"data\":\"aGVsbG8=\"}}]}}]}")));
 }
}
