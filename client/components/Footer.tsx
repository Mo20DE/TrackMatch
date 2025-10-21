import Socials from "./Socials";

function Footer() {
    
    const currentYear = new Date().getFullYear();

    return (
        <footer className={`flex flex-row items-center justify-between
            w-full p-8 border-t
        `}>
            <p className="text-[18px]">© {currentYear} Mohammed Zain Maqsood</p>
            <div className="">
                <Socials size={28} />
            </div>
        </footer>
    )
};

export default Footer;