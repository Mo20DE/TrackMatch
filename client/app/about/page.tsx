import Image from "next/image";

const About = () => {
  return (
    <div className='flex flex-col gap-12 text-justify text-xl pl-20 pr-20'>
        <p>
            This app shows my latest coding project. It implements the Shazam algorithm 
            to recognize songs. The frontend is built with Next.js (React/Tailwind), while the 
            backend is built with Spring Boot and PostgreSQL and offers two RESTful endpoints. <br/>
        </p>
        <div className="flex justify-center">
          <Image 
                src="/architecture.png"
                alt="System Architecture"
                width={900}
                height={300}
          />
        </div>
        <p>
            One is <code>POST /api/process-url</code>, which is responsible for 
            fetching song metadata via Spotify's Web-API based on a provided Spotify-URL, downloading 
            the found songs from youtube via yt-dlp, generating fingerprints (short digital audio signatures - unique 
            patterns of certain frequencies at specific time points in a song to quickly identify and match 
            music) and finally saving the acquired data into a databse. <br/><br/>
            The other endpoint is <code>POST /api/process-audio</code>, which handles the processing of the 
            recorded audio snippet by generating it's corresponding fingerprint and applying an efficient matching
            algorithm that uses the fingerprints from the database to find the YouTube Video-Id's of the four highest 
            matches. Finally these Video-ID's are used to display the videos on the client side. 
        </p>
    </div>
  )
}

export default About;
