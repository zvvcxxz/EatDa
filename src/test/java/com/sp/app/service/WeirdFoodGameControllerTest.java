package com.sp.app.service;
import com.sp.app.controller.WeirdFoodGameController;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import com.sp.app.service.*;
class WeirdFoodGameControllerTest {
 @Test void imageRequiresOwnTicketAndCachesResult() {
  var service=mock(WeirdFoodGameService.class); var images=mock(WeirdFoodImageService.class);
  var controller=new WeirdFoodGameController(service,images); var session=new MockHttpSession();
  when(service.validatePrompt("김치 파인애플")).thenReturn("김치 파인애플");
  var result=new WeirdFoodGameService.Result("조합",60,"등급","맛","심사",List.of("김치","파인애플"),"사진");
  when(service.judge(anyString())).thenReturn(result);
  var reply=(WeirdFoodGameController.Reply)controller.judge(new WeirdFoodGameController.Combination("김치 파인애플"),session).getBody();
  var request=new WeirdFoodGameController.ImageRequest(reply.imageTicket());
  assertEquals(400,controller.image(request,new MockHttpSession()).getStatusCode().value());
  when(images.generate(result)).thenReturn(new WeirdFoodImageService.Picture(new byte[]{1},"image/png"));
  assertEquals(200,controller.image(request,session).getStatusCode().value());
  assertEquals(200,controller.image(request,session).getStatusCode().value());
  verify(images,times(1)).generate(result);
  assertEquals(429,controller.judge(new WeirdFoodGameController.Combination("김치 파인애플"),session).getStatusCode().value());
 }
}
