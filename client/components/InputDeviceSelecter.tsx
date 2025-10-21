
import { ToggleGroup, ToggleGroupItem } from "@/components/ui/toggle-group";
import { MdComputer } from "react-icons/md";
import { FaMicrophone } from "react-icons/fa";

interface InputDeviceSelecterProps {
  inputDeviceType: string,
  onSend: (value: string) => void
}

const InputDeviceSelecter = ({inputDeviceType, onSend}: InputDeviceSelecterProps) => {

  return (
    <ToggleGroup 
      type="single" 
      value={inputDeviceType} 
      defaultValue={inputDeviceType}
      onValueChange={(val) => val && onSend(val)}
      className="w-32 border-1 rounded-lg"
    >
        <ToggleGroupItem value="screen" size="lg" onClick={() => onSend("screen")}><MdComputer /></ToggleGroupItem>
        <ToggleGroupItem value="mic" size="lg" onClick={() => onSend("mic")}><FaMicrophone /></ToggleGroupItem>
    </ToggleGroup>
  )
}

export default InputDeviceSelecter