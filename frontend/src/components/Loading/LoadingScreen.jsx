import { useEffect } from "react";

function LoadingScreen({ setCurrentPage }) {

    useEffect(()=>{

        const timer = setTimeout(()=>{

            setCurrentPage("result");

        },4000);

        return ()=>clearTimeout(timer);

    },[]);

    return(

        <div
            style={{
                display:"flex",
                justifyContent:"center",
                alignItems:"center",
                height:"100vh",
                flexDirection:"column"
            }}
        >

            <h1>AI가 계약서를 분석 중입니다...</h1>

            <h2>잠시만 기다려주세요.</h2>

        </div>

    );

}

export default LoadingScreen;