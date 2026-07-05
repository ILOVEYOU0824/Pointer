import { useEffect, useState } from "react";

function LoadingScreen({ setCurrentPage }) {

  const [progress, setProgress] = useState(0);

  const steps = [
    "계약서를 읽는 중...",
    "위험 조항을 분석하는 중...",
    "법률 기준과 비교하는 중...",
    "위험도를 계산하는 중...",
    "AI 요약을 생성하는 중..."
  ];

  const [stepIndex, setStepIndex] = useState(0);

  useEffect(() => {

    const timer = setInterval(() => {

      setProgress(prev => {

        if (prev >= 100) {

          clearInterval(timer);

          setTimeout(() => {

            setCurrentPage("result");

          },500);

          return 100;

        }

        return prev + 2;

      });

    },80);

    return ()=>clearInterval(timer);

  },[]);

  useEffect(()=>{

    if(progress<20) setStepIndex(0);
    else if(progress<40) setStepIndex(1);
    else if(progress<60) setStepIndex(2);
    else if(progress<80) setStepIndex(3);
    else setStepIndex(4);

  },[progress]);

  return (

    <div
      style={{
        display:"flex",
        justifyContent:"center",
        alignItems:"center",
        flexDirection:"column",
        height:"100vh"
      }}
    >

      <h1>🤖 AI 분석 중</h1>

      <h3>{steps[stepIndex]}</h3>

      <progress
        value={progress}
        max="100"
        style={{
          width:"500px",
          height:"25px",
          marginTop:"40px"
        }}
      />

      <h2>{progress}%</h2>

    </div>

  );

}

export default LoadingScreen;