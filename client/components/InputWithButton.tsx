"use client";
import { Label } from '@/components/ui/label'
import { Input } from "@/components/ui/input"
import { Button } from "@/components/ui/button"
import { useState } from 'react'


type InputWithButtonProps = {
    setInputBody: (value: string) => void,
    setCanSend: (value: boolean) => void
    disabled: boolean
}

const InputWithButton = ({setInputBody, setCanSend, disabled}: InputWithButtonProps) => {

    const [url, setUrl] = useState("")
    const [error, setError] = useState("")

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (url.trim() == "") {
            setError("Please enter a spotify URL");
            return;
        }
        const dest_url = new URL(url);
        if (!url.startsWith("https://") || 
            dest_url.hostname !== "open.spotify.com" || 
            !(dest_url.pathname && dest_url.pathname !== "/"))
        {
            setError("Please enter a valid spotify URL");
            return;
        }
        if ((!url.includes("track") && !url.includes("album") && !url.includes("playlist"))) {
            setError("Please provide a track, album or playlist");
            return;
        }
        setInputBody(url);
        setCanSend(true);
        setError("");
        setUrl("");
    }
    
  return (
    <form 
        onSubmit={handleSubmit}
        className='grid min-w-lg items-center gap-3'
    >
        <Label htmlFor='url'>Add new song, album or playlist</Label> 
        <div className='flex flex-row gap-2 w-'>
            <Input 
                type="url" 
                id='url' 
                placeholder="https://open.spotify.com/.../..." 
                value={url}
                onChange={(e) => {
                    setUrl(e.target.value);
                    if (error) setError("");
                }}
                className={error ? "border-red-500 focus-visible:ring-red-500" : ""}
                disabled={disabled}
            />
            <Button type="submit" variant="outline" disabled={disabled}>
                Add Songs
            </Button>
        </div>
        {error && <p className='text-red-600 text-sm'>{error}</p>}
    </form>
  )
}

export default InputWithButton
