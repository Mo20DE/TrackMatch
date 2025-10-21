import { FaLinkedin, FaGithub } from 'react-icons/fa'
import { FaSquareXTwitter } from 'react-icons/fa6';
import { RiMailSendLine } from 'react-icons/ri';

interface SocialsProps {
    size: number,
    add_mail?: boolean
}

const Socials = ({ size, add_mail=false }: SocialsProps) => {
    return (
        <div className="flex flex-row gap-5">
            {/* #057ff4 */}
            <a 
                href='https://www.linkedin.com/in/mohammed-zain-maqsood-2874a8212' 
                target='_blank' 
                className='
                    text-black 
                    hover:text-[#1c91ff] 
                    dark:text-white
                    dark:hover:text-[#1c91ff] 
                    hover:scale-110
                    duration-300 
                    transition-all 
                    will-change-transform'
            >
                <FaLinkedin size={size}/>
            </a>
            <a 
                href='https://x.com/MZM_DE' 
                target='_blank' 
                className='
                    text-black 
                    hover:text-gray-500
                    dark:text-white
                    dark:hover:text-gray-400
                    hover:scale-110
                    duration-300 
                    transition-all 
                    will-change-transform'
                >
                    <FaSquareXTwitter size={size}/>
            </a>
            <a 
                href='https://github.com/Mo20DE' 
                target='_blank' 
                className='
                    text-black 
                    hover:text-[#d364ff] 
                    dark:text-white
                    dark:hover:text-[#d364ff] 
                    hover:scale-110
                    duration-300 
                    transition-all 
                    will-change-transform'
                >
                    <FaGithub size={size}/>
            </a>
            {add_mail && <a 
                href='mailto:zain.maqsood2018@gmx.de?subject=Hey Mohammed 👋'
                className='
                    text-black 
                    dark:text-white
                    hover:scale-110
                    duration-300 
                    transition-all 
                    will-change-transform'
            >
                <RiMailSendLine size={size}/>
            </a>}
        </div>
    );
};

export default Socials;
