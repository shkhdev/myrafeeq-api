import http from "k6/http";
import { check, sleep } from "k6";
import { Rate, Trend } from "k6/metrics";

const errorRate = new Rate("errors");
const dashboardDuration = new Trend("dashboard_duration");
const prayerTimesDuration = new Trend("prayer_times_duration");

const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";
const AUTH_TOKEN = __ENV.AUTH_TOKEN || "";

export const options = {
  scenarios: {
    // Scenario 1: Normal load (baseline)
    normal: {
      executor: "constant-arrival-rate",
      rate: 100,
      timeUnit: "1s",
      duration: "2m",
      preAllocatedVUs: 50,
      maxVUs: 200,
    },

    // Scenario 2: Prayer time spike (simulates Fajr/Dhuhr/Asr/Maghrib/Isha)
    prayer_spike: {
      executor: "ramping-arrival-rate",
      startRate: 100,
      timeUnit: "1s",
      stages: [
        { duration: "30s", target: 100 },
        { duration: "10s", target: 1000 },
        { duration: "2m", target: 1000 },
        { duration: "30s", target: 100 },
      ],
      preAllocatedVUs: 200,
      maxVUs: 1000,
      startTime: "3m",
    },

    // Scenario 3: Ramadan peak (x10 traffic)
    ramadan: {
      executor: "ramping-arrival-rate",
      startRate: 100,
      timeUnit: "1s",
      stages: [
        { duration: "1m", target: 500 },
        { duration: "3m", target: 2000 },
        { duration: "1m", target: 500 },
      ],
      preAllocatedVUs: 500,
      maxVUs: 2000,
      startTime: "7m",
    },
  },

  thresholds: {
    http_req_duration: ["p(95)<500", "p(99)<1000"],
    errors: ["rate<0.01"],
    dashboard_duration: ["p(95)<300"],
    prayer_times_duration: ["p(95)<200"],
  },
};

const authHeaders = {
  Authorization: `Bearer ${AUTH_TOKEN}`,
  "Content-Type": "application/json",
};

const publicHeaders = {
  "Content-Type": "application/json",
};

export default function () {
  const scenario = __ITER % 10;

  if (scenario < 5) {
    // 50% — Dashboard (main screen, authenticated)
    const start = Date.now();
    const res = http.get(`${BASE_URL}/api/v1/dashboard`, {
      headers: authHeaders,
    });
    dashboardDuration.add(Date.now() - start);
    check(res, { "dashboard 200": (r) => r.status === 200 });
    errorRate.add(res.status !== 200);
  } else if (scenario < 7) {
    // 20% — Prayer times by location (public)
    const start = Date.now();
    const res = http.get(
      `${BASE_URL}/api/v1/prayer-times/by-location?lat=41.2995&lon=69.2401&timezone=Asia/Tashkent&method=MBOUZ&madhab=HANAFI`,
      { headers: publicHeaders },
    );
    prayerTimesDuration.add(Date.now() - start);
    check(res, { "prayer-times 200": (r) => r.status === 200 });
    errorRate.add(res.status !== 200);
  } else if (scenario < 9) {
    // 20% — City search (public)
    const queries = ["Tashkent", "Samarkand", "Bukhara", "Namangan", "Andijan"];
    const q = queries[Math.floor(Math.random() * queries.length)];
    const res = http.get(`${BASE_URL}/api/v1/cities?q=${q}&limit=10`, {
      headers: publicHeaders,
    });
    check(res, { "cities 200": (r) => r.status === 200 });
    errorRate.add(res.status !== 200);
  } else {
    // 10% — Toggle prayer (authenticated, write)
    const prayers = ["FAJR", "DHUHR", "ASR", "MAGHRIB", "ISHA"];
    const prayer = prayers[Math.floor(Math.random() * prayers.length)];
    const today = new Date().toISOString().split("T")[0];
    const res = http.post(
      `${BASE_URL}/api/v1/prayer-tracking/toggle`,
      JSON.stringify({ date: today, prayer: prayer, prayed: true }),
      { headers: authHeaders },
    );
    check(res, { "toggle 200/409": (r) => r.status === 200 || r.status === 409 });
    errorRate.add(res.status !== 200 && res.status !== 409);
  }

  sleep(0.1);
}
