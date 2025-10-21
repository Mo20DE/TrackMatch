import { Alert, AlertTitle } from "@/components/ui/alert";
import { CheckCircle2Icon, AlertCircleIcon } from "lucide-react";
import { motion, AnimatePresence } from "framer-motion";

interface CustomAlertProps {
    message: string, 
    show: boolean, 
    destructive: boolean
};

const CustomAlert = ({message, show, destructive}: CustomAlertProps) => {
    return (
        <AnimatePresence>
            {show && (<motion.div
                key="alert"
                initial={{opacity: 0, y: -20}}
                animate={{opacity: 1, y: 20}}
                exit={{opacity: 0, y: 0}}
                transition={{duration: 0.2}} 
                className="fixed top-13 left-1/2 -translate-x-1/2 z-50 w-fit max-w-md"
            >
                <Alert variant={destructive ? "destructive" : "default"}>
                    {destructive ? <AlertCircleIcon /> : <CheckCircle2Icon />}
                    <AlertTitle>{message}</AlertTitle>
                </Alert>
            </motion.div>
            )}
        </AnimatePresence>
    )
}

export default CustomAlert;
