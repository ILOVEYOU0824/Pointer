import { useState } from "react";
import "./App.css";

import UploadBox from "./components/Upload/UploadBox";
import LoadingScreen from "./components/Loading/LoadingScreen";
import ResultPage from "./components/Result/ResultPage";

function App() {
  const [currentPage, setCurrentPage] = useState("upload");
  const [selectedFile, setSelectedFile] = useState(null);
  const [analysisResult, setAnalysisResult] = useState(null);
  const [error, setError] = useState(null);

  const handleAnalyzeStart = () => {
    setError(null);
    setAnalysisResult(null);
    setCurrentPage("loading");
  };

  const handleAnalysisComplete = (result) => {
    setAnalysisResult(result);
    setCurrentPage("result");
  };

  const handleAnalysisError = (message) => {
    setError(message);
    setCurrentPage("upload");
  };

  const handleReset = () => {
    setSelectedFile(null);
    setAnalysisResult(null);
    setError(null);
    setCurrentPage("upload");
  };

  return (
    <>
      {currentPage === "upload" && (
        <UploadBox
          selectedFile={selectedFile}
          setSelectedFile={setSelectedFile}
          onAnalyzeStart={handleAnalyzeStart}
          error={error}
        />
      )}

      {currentPage === "loading" && (
        <LoadingScreen
          selectedFile={selectedFile}
          onComplete={handleAnalysisComplete}
          onError={handleAnalysisError}
        />
      )}

      {currentPage === "result" && (
        <ResultPage
          analysisResult={analysisResult}
          onReset={handleReset}
        />
      )}
    </>
  );
}

export default App;
