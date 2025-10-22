
interface TotalSongsProps {
    totalSongs: number
};

const TotalSongs = ({totalSongs}: TotalSongsProps) => {
    return (
        <div className="absolute top-[-30] right-[-50] bg-white text-black font-bold rounded-full p-2 pl-4 pr-4 text-[14px]">
            {totalSongs} Songs
        </div>
    )
}

export default TotalSongs;
