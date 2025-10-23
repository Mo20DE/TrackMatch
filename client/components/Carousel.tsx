import React from 'react'

interface CarouselProps {
    matchYtIds: string[]
}

const baseUrl = "https://www.youtube.com/embed/";

const Carousel = ({matchYtIds}: CarouselProps) => {

  return (
    <div className="carousel carousel-center bg-neutral rounded-box max-w-2xl space-x-4 p-4">
        {matchYtIds.map((url, index) => {
        return (
            <iframe key={index} className="carousel-item w-md h-72 rounded-xl" src={baseUrl + url} allow='fullscreen' allowFullScreen/>
        )})}
    </div>
  )
}

export default Carousel;
