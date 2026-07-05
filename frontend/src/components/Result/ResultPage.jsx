function ResultPage({ selectedFile }) {

    return(

        <div
            style={{
                display:"flex",
                justifyContent:"center",
                alignItems:"center",
                flexDirection:"column",
                height:"100vh"
            }}
        >

            <h1>분석 완료!</h1>

            <h2>

                {selectedFile?.name}

            </h2>

            <h3>여기에 결과가 들어갑니다.</h3>

        </div>

    );

}

export default ResultPage;