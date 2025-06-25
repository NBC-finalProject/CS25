import http from 'k6/http';
import { check } from 'k6';

export let options = {
  vus: 500,
  duration: '60s',
};

export default function () {
  const answerId = Math.floor(Math.random() * 1000) + 1;
  const url = `http://host.docker.internal:8080/quizzes/${answerId}/feedback`;

  const res = http.get(url, {
    headers: {
      Accept: 'text/event-stream',
    },
    timeout: '60s',
  });

  check(res, {
    'status is 200': (r) => r.status === 200,
  });
}
