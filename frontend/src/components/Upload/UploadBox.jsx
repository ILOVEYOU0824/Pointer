import "./UploadBox.css";

function UploadBox({
  selectedFile,
  setSelectedFile,
  setCurrentPage,
}) {
  const handleFileChange = (e) => {
    if (e.target.files.length > 0) {
      setSelectedFile(e.target.files[0]);
    }
  };

  return (
    <div className="upload-container">
      <div className="upload-card">
        <h1>SafeContract AI</h1>

        <p className="subtitle">
          AI 계약서 위험도 분석 시스템
        </p>

        <div className="upload-area">
          <div className="pdf-icon">📄</div>

          <h2>계약서 PDF 업로드</h2>

          <p>
            PDF 파일을 업로드하면 AI가 위험 조항을 분석합니다.
          </p>

          <label className="file-btn">
            파일 선택
            <input
              type="file"
              accept=".pdf"
              hidden
              onChange={handleFileChange}
            />
          </label>

          {selectedFile && (
            <div className="file-info">
              <p>선택된 파일</p>
              <strong>{selectedFile.name}</strong>
            </div>
          )}

          <button
            disabled={!selectedFile}
            onClick={() => setCurrentPage("loading")}
          >
            AI 분석 시작
          </button>
        </div>
      </div>
    </div>
  );
}

export default UploadBox;