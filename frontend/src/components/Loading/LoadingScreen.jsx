import { useEffect, useState } from "react";
import { analyzePdf } from "../../api/analyze";
import "./LoadingScreen.css";

function LoadingScreen({ selectedFile, onComplete, onError }) {
  const [progress, setProgress] = useState(0);
  const [statusMessage, setStatusMessage] = useState("");

  const steps = [
    "계약서를 읽는 중...",
    "위험 조항을 분석하는 중...",
    "법률 기준과 비교하는 중...",
    "위험도를 계산하는 중...",
    "AI 요약을 생성하는 중...",
  ];

  const [stepIndex, setStepIndex] = useState(0);

  useEffect(() => {
    let cancelled = false;

    const progressTimer = setInterval(() => {
      setProgress((prev) => {
        if (prev >= 95) return prev;
        return prev + 1;
      });
    }, 300);

    const runAnalysis = async () => {
      try {
        const result = await analyzePdf(selectedFile, {
          onRetry: (attempt, max) => {
            if (!cancelled) {
              setStatusMessage(`AI 서버 혼잡으로 재시도 중... (${attempt}/${max})`);
            }
          },
        });
        if (cancelled) return;

        setProgress(100);
        setStatusMessage("");
        setTimeout(() => {
          if (!cancelled) onComplete(result);
        }, 500);
      } catch (err) {
        if (!cancelled) onError(err.message);
      }
    };

    runAnalysis();

    return () => {
      cancelled = true;
      clearInterval(progressTimer);
    };
  }, [selectedFile, onComplete, onError]);

  useEffect(() => {
    if (progress < 20) setStepIndex(0);
    else if (progress < 40) setStepIndex(1);
    else if (progress < 60) setStepIndex(2);
    else if (progress < 80) setStepIndex(3);
    else setStepIndex(4);
  }, [progress]);

  return (
    <div className="loading-container">
      <h1>🤖 AI 분석 중</h1>
      <h3>{statusMessage || steps[stepIndex]}</h3>
      <p className="loading-file">{selectedFile?.name}</p>

      <progress value={progress} max="100" className="loading-progress" />
      <h2>{progress}%</h2>

      {statusMessage && (
        <p className="loading-retry-hint">잠시만 기다려 주세요. 자동으로 재시도합니다.</p>
      )}
    </div>
  );
}

export default LoadingScreen;
