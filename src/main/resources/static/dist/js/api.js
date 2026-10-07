/**
 * 백엔드 연동 지점. 실제 API 경로는 팀에서 확정 후 null을 함수로 교체합니다.
 * 예시 데이터/임의의 API 주소/인증 우회 코드는 없습니다.
 * 화면에서는 api.call('recipes', 조건)처럼 호출합니다.
 */
const handlers = {
  ingredients: null,
  recipes: null,
  recipeDetail: null,
  fridge: null,
  communityHighlights: null,
  posts: null,
  postDetail: null,
  savePost: null,
  deletePost: null,
  togglePostLike: null,
  participate: null,
};
export const api = {
  connected(name) {
    return typeof handlers[name] === "function";
  },
  async call(name, payload, options = {}) {
    if (!this.connected(name)) {
      const error = new Error("서버 연결 전입니다.");
      error.code = "NOT_CONNECTED";
      throw error;
    }
    return handlers[name](payload, options);
  },
  // 개발자가 컨트롤러 경로 확정 후 등록. 사용자 입력으로 함수를 만들지 않습니다.
  register(name, handler) {
    if (!(name in handlers) || typeof handler !== "function")
      throw new Error("API 등록값을 확인하세요.");
    handlers[name] = handler;
  },
};

export async function request(url, { method = "GET", body, signal } = {}) {
  const headers = { Accept: "application/json" };
  if (body !== undefined) headers["Content-Type"] = "application/json";
  const token = document.querySelector('meta[name="_csrf"]')?.content;
  const header = document.querySelector('meta[name="_csrf_header"]')?.content;
  if (
    token &&
    header &&
    !["GET", "HEAD", "OPTIONS"].includes(method.toUpperCase())
  )
    headers[header] = token;
  const response = await fetch(url, {
    method,
    headers,
    signal,
    credentials: "same-origin",
    body: body === undefined ? undefined : JSON.stringify(body),
  });
  if (response.redirected || !response.ok)
    throw new Error("요청에 실패했습니다. 로그인과 서버 상태를 확인하세요.");
  if (response.status === 204) return null;
  if (
    !(response.headers.get("content-type") || "").includes("application/json")
  )
    throw new Error(
      "JSON 응답이 아닙니다. 로그인 이동/응답 형식을 확인하세요.",
    );
  return response.json();
}
