
interface ListenButtonProps {
    isRecording: boolean,
    setListenBtnClicked: (value: boolean) => void
}

const ListenButton = ({isRecording, setListenBtnClicked}: ListenButtonProps) => {
    
    return (
        <div className="relative w-34 h-34">
            {isRecording && (
                <>
                    <span className="absolute inset-0 rounded-full bg-blue-400 animate-ripple"></span>
                    <span className="absolute inset-0 rounded-full bg-blue-400 animate-ripple animation-delay-1"></span>
                    <span className="absolute inset-0 rounded-full bg-blue-400 animate-ripple animation-delay-2"></span>
                </>
            )}
            <button 
                className="absolute w-full h-full z-20 bg-blue-700 font-bold hover:bg-blue-600 transition rounded-full"
                onClick={() => setListenBtnClicked(true)}
            >
                {isRecording ? "Listening..": "Listen"}
            </button>
        </div>

    )
}

export default ListenButton;