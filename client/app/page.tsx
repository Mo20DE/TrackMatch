"use client";

import { useState, useEffect } from "react";

import ListenButton from "@/components/ListenButton";
import InputDeviceSelecter from "@/components/InputDeviceSelecter";
import InputWithButton from "@/components/InputWithButton";
import CustomAlert from "@/components/CustomAlert";
import TotalSongs from "@/components/TotalSongs";
import Carousel from "@/components/Carousel";
import DownloadSpinner from "@/components/DownloadSpinner";
import { Button } from "@/components/ui/button";


import { AudioRecorderService } from "@/utils/AudioRecorderService";

import { Client, IMessage } from "@stomp/stompjs";

const recordingTime = 10000;
const BACKEND_URL = "http://localhost:8080"
const audioRecorderService = new AudioRecorderService();

export default function Home() {

  const [totalSongs, setTotalSongs] = useState(0);
  const [inputDeviceType, setInputDeviceType] = useState("screen"); // default input device
  const [listenBtnClicked, setListenBtnClicked] = useState(false);
  const [isRecording, setIsRecording] = useState(false);
  const [inputBody, setInputBody] = useState("");
  const [canSend, setCanSend] = useState(false);
  const [processStage, setProcessStage] = useState("");

  const [notif, setNotif] = useState("");
  const [showAlert, setShowAlert] = useState(false);
  const [inputDisabled, setInputDisabled] = useState(false);
  const [destructive, setDestructive] = useState(false);
  const [matchYtIds, setMatchYtIds] = useState<string[]>([]);
  const [showCarousel, setShowCarousel] = useState(false);


  useEffect(() => {
    if (showAlert) {
      const timer = setTimeout(() => {
        setNotif("");
        setShowAlert(false);
        setDestructive(false);
      }, 2000)
      return () => clearTimeout(timer)
    }
  }, [showAlert])

  useEffect(() => {
    let clientInstance: Client | null = null;
    const initWebSocket = async () => {
      const { default: SockJS } = await import("sockjs-client");
      const socket = new SockJS(`${BACKEND_URL + "/ws"}`);
      const client = new Client({
        webSocketFactory: () => socket,
        debug: (str) => console.log(str),
        reconnectDelay: 5000,
        onConnect: () => {
          console.log("Connected to Websocket!")
          client.subscribe("/topic/default", (msg: IMessage) => {
            setTotalSongs(Number(msg.body));
          })
          client.subscribe("/topic/number-total-songs", (msg: IMessage) => {
            setTotalSongs(Number(msg.body));
          })
          client.subscribe("/topic/process-status", (msg: IMessage) => {
            const content = msg.body;
            setProcessStage(content);
            if (content.includes("done") || content.includes("error")) {
              setProcessStage("");
            }
          })

          client.subscribe("/topic/download-status", (msg) => {
            setShowAlert(false);
            setNotif(msg.body)
            setShowAlert(true);
          })
        },
        onStompError: (frame) => console.error("STOMP Error: ", frame)
      })
      client.activate();
      clientInstance = client;
    }
    initWebSocket();
    return () => {
      if (clientInstance) clientInstance.deactivate();
    }
  }, [])


  useEffect(() => {

    if (!inputBody || !canSend) return;
    const sendUrl = async () => {
      try {
        const response = await fetch(
          `${BACKEND_URL}/api/process-url`,
          {
            method: "POST",
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({url: inputBody.trim()})
          }
        )
        if (!response.ok) {
          throw new Error(`HTTP error! Status: " ${response.status}`);
        }
        const data = await response.json();
        console.log("Response: ", data );
        
      } catch (err) {
        console.error("Error during sending: ", err);
        setNotif("Provided URL could not be found")
        setShowAlert(true);
        setDestructive(true);
        
      } finally {
        setCanSend(false);
      }
    }
    sendUrl(); // send the spotify url to the backend
  }, [inputBody, canSend])

  useEffect(() => {
    if (matchYtIds.length > 0) {
      setShowCarousel(true);
    }
  }, [matchYtIds])


  useEffect(() => {

    const sendAudioData = async () => {
      try {
        // start recording
        setInputDisabled(true);
        await audioRecorderService.startRecording(inputDeviceType);
        setIsRecording(true);

        await new Promise(resolve => setTimeout(resolve, recordingTime));
        const base64Url = await audioRecorderService.stopRecording();

        if (!base64Url) {
          console.warn("Recording stopped, but Base64-String is empty.");
          return;
        }
        const base64Content = base64Url.split(",")[1];

        const response = await fetch(
          `${BACKEND_URL}/api/process-audio`,
          {
            method: "POST",
            headers: {'Content-Type': 'application/json'},
            body: JSON.stringify({audio: base64Content})
          }
        )
        const msg = await response.text();
        if (!response.ok) {
          console.warn("Server returned error: ", response.status, msg);
          setNotif(msg);
          setShowAlert(true);
          setDestructive(true);
        } 
        else if (response.status === 204) {
          setNotif("Please add atleast two songs first");
          setShowAlert(true);
          setDestructive(true);
        }
        else {
          setMatchYtIds([]);
          const result = msg.split(",").filter(Boolean);
          setMatchYtIds(prev => [...prev, ...result]);
        }
      } 
      catch (err) {
        console.error("Unexpected error during sending: ", err)
      }
      finally {
        setListenBtnClicked(false); // reset flag
        setInputDisabled(false);
        setIsRecording(false);
      }
    }

    if (listenBtnClicked) {
      sendAudioData();
      return;
    }
  }, [listenBtnClicked])

  return (
    <div className="flex flex-col relative justify-center items-center gap-22">
      <CustomAlert message={notif} show={showAlert} destructive={destructive}/>
      <TotalSongs totalSongs={totalSongs}/>
      <p>Identify and discover songs in seconds.</p>
      <ListenButton isRecording={isRecording} setListenBtnClicked={setListenBtnClicked}/>
      <div className="flex flex-col items-center gap-14">
        <InputDeviceSelecter inputDeviceType={inputDeviceType} onSend={setInputDeviceType}/>
        {matchYtIds.length > 0 && showCarousel && (
          <div className="flex flex-col">
            <p>{showCarousel}</p>
            <Button variant="secondary" size="lg" className='ml-4 w-fit' onClick={() => setShowCarousel(false)}>Close</Button>
            <Carousel matchYtIds={matchYtIds} />
          </div>
        )}
        <InputWithButton setInputBody={setInputBody} setCanSend={setCanSend} disabled={inputDisabled || processStage.length > 0}/>
        {processStage.length > 0 && <DownloadSpinner processStage={processStage}/>}
      </div>
    </div>
  );
}
