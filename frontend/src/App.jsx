import { useState } from "react";
import "./App.css";

import UploadBox from "./components/Upload/UploadBox";
import LoadingScreen from "./components/Loading/LoadingScreen";
import ResultPage from "./components/Result/ResultPage";

function App() {
  // upload | loading | result
  const [currentPage, setCurrentPage] = useState("upload");

  const [selectedFile, setSelectedFile] = useState(null);

  return (
    <>
      {currentPage === "upload" && (
        <UploadBox
          selectedFile={selectedFile}
          setSelectedFile={setSelectedFile}
          setCurrentPage={setCurrentPage}
        />
      )}

      {currentPage === "loading" && (
        <LoadingScreen
          setCurrentPage={setCurrentPage}
        />
      )}

      {currentPage === "result" && (
        <ResultPage
          selectedFile={selectedFile}
        />
      )}
    </>
  );
}

export default App;