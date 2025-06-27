import http from 'k6/http';

export const options = {
  vus: 50,              // 동시 실행자 수 (스레드 개념)
  iterations: 9900,     // 총 요청 수
};

export default function () {
  const subscriptionId = __ITER + 10; // 1부터 10000까지
  http.get(
      `http://host.docker.internal:8080/accuracyTest/getTodayQuiz/${subscriptionId}`);

}