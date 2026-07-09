const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "";
const MAX_ATTEMPTS = 3;
const RETRY_DELAY_MS = 20000;

function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

function formatErrorMessage(message) {
  if (!message) return "분석에 실패했습니다.";

  const lower = message.toLowerCase();

  if (lower.includes("high demand") || lower.includes("try again") || lower.includes("혼잡")) {
    return "AI 서버가 일시적으로 혼잡합니다. 잠시 후 다시 시도해 주세요.";
  }
  if (lower.includes("quota") || lower.includes("한도")) {
    return "AI 일일 사용 한도(20회)를 초과했습니다. 내일 다시 시도하거나 Google AI Studio에서 한도를 확인해 주세요.";
  }

  return message;
}

function isRetryable(message) {
  const lower = (message || "").toLowerCase();
  return lower.includes("혼잡") || lower.includes("high demand") || lower.includes("try again");
}

async function analyzePdfOnce(file) {
  const formData = new FormData();
  formData.append("file", file);

  const response = await fetch(`${API_BASE_URL}/api/analyze`, {
    method: "POST",
    body: formData,
  });

  const data = await response.json();

  if (!response.ok) {
    throw new Error(formatErrorMessage(data.message));
  }

  return data;
}

export async function analyzePdf(file, { onRetry } = {}) {
  let lastError;

  for (let attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
    try {
      return await analyzePdfOnce(file);
    } catch (error) {
      lastError = error;
      if (!isRetryable(error.message) || attempt === MAX_ATTEMPTS) {
        throw error;
      }
      onRetry?.(attempt, MAX_ATTEMPTS);
      await sleep(RETRY_DELAY_MS);
    }
  }

  throw lastError;
}
