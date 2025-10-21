import Image from "next/image";

const About = () => {
  return (
    <div className='text-justify text-xl pl-20 pr-20'>
        <p>
            This app shows my latest coding project. It implements the Shazam algorithm 
            to recognize music. The frontend is built with Next.js and the backend with Spring 
            Boot and PostgreSQL for the database. The backend offers 
            two endpoints. One handles fetching spotify data, downloading songs and generating 
            fingerprints for the songs. The other endpoint is responsible for processing the recorded 
            audio by generating it's fingerprint and matching it with the stored fingerprints in database.
        </p>
        <br/>
        <p>The following graphic shows the system architecture of the project:</p>
        <div className="mt-10 flex justify-center">
          <Image 
                src="/architecture.png"
                alt="System Architecture"
                width={900}
                height={300}
          />
        </div>
    </div>
  )
}

export default About;