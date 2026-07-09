import { CircularProgressbar, buildStyles } from "react-circular-progressbar";
import "react-circular-progressbar/dist/styles.css";
import "./ResultPage.css";

const SCORE_COLORS = {
  안전: "#4caf50",
  보통: "#2196f3",
  주의: "#ff9800",
  위험: "#f44336",
};

const SEVERITY_LABELS = {
  high: "높음",
  medium: "보통",
  low: "낮음",
};

function ResultPage({ analysisResult, onReset }) {
  if (!analysisResult) return null;

  const { fileName, score, scoreLabel, summary, toxicClauses } = analysisResult;
  const scoreColor = SCORE_COLORS[scoreLabel] || "#2196f3";

  return (
    <div className="result-container">
      <div className="result-card">
        <h1>분석 완료</h1>
        <p className="result-filename">{fileName}</p>

        <div className="score-section">
          <div className="score-circle">
            <CircularProgressbar
              value={score}
              text={`${score}`}
              styles={buildStyles({
                textSize: "24px",
                pathColor: scoreColor,
                textColor: scoreColor,
                trailColor: "#e0e0e0",
              })}
            />
          </div>
          <div className="score-info">
            <span className="score-label" style={{ color: scoreColor }}>
              {scoreLabel}
            </span>
            <p className="score-desc">계약서 안전도 점수</p>
          </div>
        </div>

        <div className="summary-section">
          <h2>AI 요약</h2>
          <p>{summary}</p>
        </div>

        <div className="clauses-section">
          <h2>
            독소조항 {toxicClauses.length > 0 ? `(${toxicClauses.length}건)` : ""}
          </h2>

          {toxicClauses.length === 0 ? (
            <div className="no-clauses">
              발견된 독소조항이 없습니다.
            </div>
          ) : (
            <div className="clauses-list">
              {toxicClauses.map((clause) => (
                <div key={clause.id} className={`clause-card severity-${clause.severity}`}>
                  <div className="clause-header">
                    <h3>{clause.title}</h3>
                    <span className={`severity-badge ${clause.severity}`}>
                      {SEVERITY_LABELS[clause.severity] || clause.severity}
                    </span>
                  </div>
                  <blockquote className="clause-text">
                    "{clause.originalText}"
                  </blockquote>
                  <p className="clause-reason">
                    <strong>위험 이유:</strong> {clause.reason}
                  </p>
                  {clause.suggestion && (
                    <p className="clause-suggestion">
                      <strong>개선 제안:</strong> {clause.suggestion}
                    </p>
                  )}
                  {clause.page && (
                    <span className="clause-page">p.{clause.page}</span>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>

        <button className="reset-btn" onClick={onReset}>
          다른 계약서 분석하기
        </button>
      </div>
    </div>
  );
}

export default ResultPage;
