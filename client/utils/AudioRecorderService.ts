
export class AudioRecorderService {

    private mediaRecorder: MediaRecorder | null = null;
    private audioChunks: Blob[] = [];
    private stream: MediaStream | null = null;
    public isRecording: boolean = false;
    private recordingPromise: Promise<string | null> | null = null;
    private resolveRecording:((value: string | null) => void) | null = null;
    
    public async startRecording(inputDeviceType: string = "screen"): Promise<void> {
        if (this.isRecording) {
            console.log("Recording is already running.");
            return;
        }
        try {
            if (inputDeviceType === "screen") {
                this.stream = await navigator.mediaDevices.getDisplayMedia({video: true, audio: true});
                const audioTracks = this.stream.getAudioTracks();
                const audioStream = new MediaStream(audioTracks);
                this.mediaRecorder = new MediaRecorder(audioStream);
            }
            else {
                this.stream = await navigator.mediaDevices.getUserMedia({audio: true});
                this.mediaRecorder = new MediaRecorder(this.stream);
            }
            this.audioChunks = [];
            this.mediaRecorder.ondataavailable = (e: BlobEvent) => {
                if (e.data.size > 0) {
                    this.audioChunks.push(e.data);
                }
            }
            this.recordingPromise = new Promise<string | null>((resolve) => {
                this.resolveRecording = resolve;
            })
            this.mediaRecorder.onstop = async () => {
                const base64Url = await this.handleRecordingStop();
                if (this.resolveRecording) {
                    this.resolveRecording(base64Url);
                }

            }
            this.mediaRecorder.start();
            this.isRecording = true;
            console.log("Recording started...");
        } catch (error) {
            this.isRecording = false;
            const err = error as DOMException;
            throw new Error(`Microphone Error: ${err.name}. Access denied or not available.`);
        }
    }

    public stopRecording(): Promise<string | null> {
        if (!this.isRecording || this.mediaRecorder?.state === "inactive") {
            return Promise.resolve(null);
        }
        this.mediaRecorder!.stop();
        this.stream?.getTracks().forEach(track => track.stop());
        this.isRecording = false;
        console.log("Recording stopped. Converting audio to base64...");
        return this.recordingPromise!;
    }

    private handleRecordingStop(): Promise<string | null> {
        return new Promise((resolve, reject) => {
            if (!this.mediaRecorder) {
                reject("MediaRecorder not initialized.");
                return;
            }
            const mimeType: string = this.mediaRecorder.mimeType || "audio/webm";
            const audioBlob = new Blob(this.audioChunks, {type: mimeType});
            const reader = new FileReader();
            reader.onloadend = () => {
                const base64DataUrl = reader.result as string;
                resolve(base64DataUrl);
            }
            reader.onerror = (error) => {
                console.log("Error during reading the blob: ", error);
                reject(error);
            }
            reader.readAsDataURL(audioBlob);
        })
    }
}
