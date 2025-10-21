import Link from 'next/link';
import { FaGithub } from "react-icons/fa";

const NavBar = () => {
  return (
    <div className="navbar border-b h-18" >
        <div className="navbar-start">
            <div className="dropdown">
                <div tabIndex={0} role="button" className="btn btn-ghost btn-circle">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-8 w-8" fill="none" viewBox="0 0 24 24" stroke="currentColor"> <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 6h16M4 12h16M4 18h7" /> </svg>
            </div>
                <ul
                    className="menu menu-sm dropdown-content bg-base-100 rounded-box z-1 mt-3 w-52 p-2">
                    <li><Link href="/">TrackMatch</Link></li>
                    <li><Link href="/about">About</Link></li>
                </ul>
            </div>
        </div>
        <div className="navbar-center">
            <div className="font-bold text-3xl">TrackMatch</div>
        </div>
        <div className="navbar-end">
            <Link
                className="btn btn-ghost btn-circle" 
                href="https://github.com/Mo20DE/TrackMatch" 
                target='_blank'
            >
                <FaGithub size={40}/>
            </Link>
        </div>
    </div>
  )
}

export default NavBar;
